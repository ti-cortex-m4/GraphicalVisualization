package com.org.practicum.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.org.practicum.neo4j.AutocompleteName;
import com.org.practicum.neo4j.ColorCode;
import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.neo4j.ElementList;
import com.org.practicum.tree.GraphElements;

@Component
public class Neo4jDimDAO {
	private DataSource dataSourceNeo;
	private JdbcTemplate jdbcTemplate;
	RestAPI graphDb = new RestAPIFacade("http://localhost:7474/db/data");
	QueryEngine<Map<String, Object>> engine = new RestCypherQueryEngine(graphDb);
	final String SERVER_ROOT_URI = "http://localhost:7474";

	private static enum RelTypes implements RelationshipType {
		SURROGATE, HIERARCHY;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public DataSource getDataSourceNeo() {
		return dataSourceNeo;
	}

	@Autowired
	public void setDataSource(DataSource dataSourceNeo) {
		this.dataSourceNeo = dataSourceNeo;
		this.jdbcTemplate = new JdbcTemplate(dataSourceNeo);
	}
    
	/**
	 *The request parameter name is parsed to id 
     *for all the functions to perform the operations on id, 
     *the primary key for a node or row
	 * @param name
	 * @param typeElement
	 * @param typeHierarchy
	 * @return
	 */
	@SuppressWarnings("unused")
	public ElementList fetchElementsList(String name, String typeElement, String typeHierarchy) { // possible
		String queryElementId = "";
		String nameField = "";
		switch (typeElement) {
		case "Customer":
			queryElementId = "V_D_CUST_REF_CODE";
			nameField = "BK_CUST_FULL_NM";
			break;
		case "LOB":
			queryElementId = "BK_ACCT_LOB_CD";
			nameField = "BK_ACCT_LOB_CD";
			break;
		case "CountryOfRisk":
			queryElementId = "BK_CTRY_OF_RISK_ISO_CD";
			nameField = "BK_CTRY_OF_RISK_ISO_CD";
			break;
		case "LegalEntity":
			if (typeHierarchy != null) {
				if (typeHierarchy.equals("Risk")) {
					queryElementId = "BK_RSK_REF_ID";
					nameField = "BKRSKLENM";
					typeElement = "RiskLegalEntity";
				} else {
					queryElementId = "BK_LE_CD";
					nameField = "BK_LE_LDSC_TX";
				}
			}
			break;
		case "Account":
			queryElementId = "V_ACCOUNT_NUMBER";
			nameField = "BK_ACCT_NM";
			break;
		case "InvolvedParty":
			queryElementId = "BK_INVLV_PRTY_ID";
			nameField = "BK_INVLV_PRTY_NM";
			break;

		default:
			break;
		}

		String query = "MATCH (n:" + typeElement + "{" + nameField + ": '" + name + "'})RETURN distinct n";
		Iterator<Map<String, Object>> iterator = mainQueryRunMethod(query);
		ElementList listElement = new ElementList();
		while (iterator.hasNext()) {
			String sKey = "";
			Map<String, Object> row = iterator.next();
			Set<String> ElementSet = row.keySet();
			for (String NodeSetKey : ElementSet) {
				if (NodeSetKey.equals("n")) {
					Node ElementNode = (Node) row.get(NodeSetKey);
					long nodeNeoId = ElementNode.getId();
					listElement.setId((String) ElementNode.getProperty(queryElementId));
					listElement.setName(((String) ElementNode.getProperty(nameField)));
				}
			}
		}
		return listElement;
	}
    
	/**
	 * Function called to create a list of objects which are sent to reflect auto complete on front end.
     *  It matches the characters obtained from the API to the fetch names containing those characters.
	 * @param name
	 * @param typeElement
	 * @param typeHierarchy
	 * @return
	 */
	public List<AutocompleteName> fetchElementsListName(String name, String typeElement, String typeHierarchy) { // possible
		String nameField = "";
		switch (typeElement) {
		case "Customer":
			nameField = "BK_CUST_FULL_NM";
			break;

		case "LOB":
			nameField = "BK_ACCT_LOB_CD";
			break;

		case "CountryOfRisk":
			nameField = "BK_CTRY_OF_RISK_ISO_CD";
			break;

		case "LegalEntity":
			if (typeHierarchy != null) {
				if (typeHierarchy.equals("Risk")) {
					nameField = "BKRSKLENM";
					typeElement = "RiskLegalEntity";
				} else {
					nameField = "BK_LE_LDSC_TX";
				}
			}
			break;

		case "Account":
			nameField = "BK_ACCT_NM";
			break;

		case "InvolvedParty":
			nameField = "BK_INVLV_PRTY_NM";
			break;

		default:
			break;
		}
		String query = "MATCH (n:" + typeElement + ") WHERE  n." + nameField + "=~ '(?i).*" + name + ".*'  RETURN distinct n";
		Iterator<Map<String, Object>> iterator = mainQueryRunMethod(query);
		List<AutocompleteName> autocompleteList = new ArrayList<>();
		while (iterator.hasNext()) {
//			String sKey = "";
			Map<String, Object> row = iterator.next();
			Set<String> ElementSet = row.keySet();
			for (String NodeSetKey : ElementSet) {
				if (NodeSetKey.equals("n")) {
					Node ElementNode = (Node) row.get(NodeSetKey);
//					long nodeNeoId = ElementNode.getId();
					AutocompleteName nameValue = new AutocompleteName();
					nameValue.setName(((String) ElementNode.getProperty(nameField)));
					autocompleteList.add(nameValue);
				}
			}
		}
		Collections.sort(autocompleteList, new Comparator<AutocompleteName>() {
			@Override
			public int compare(AutocompleteName o1, AutocompleteName o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return autocompleteList;
	}
    /**
	 * Queries for fetching neo4j hierarchy data
	 * @param oracleRangeMap
	 * @param graphFinal
	 * @param typeElement
	 * @param id
	 * @param date
	 * @param typeHierarchy
	 * @param elementSet
	 * @param errorNodeList
	 * @return
	 * @throws ParseException
	 */
	public GraphElements searchNeo(HashMap<String, ElementBean> oracleRangeMap, GraphElements graphFinal,
			String typeElement, String id, String date, String typeHierarchy, Set<ElementBean> elementSet,
			List<ElementBean> errorNodeList) throws ParseException {
		Date myDate = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(date);
		long millisecondsDate = myDate.getTime();
		String queryElementId = "";
		String color = "";
		String nameField = "";
		switch (typeElement) {
		case "Customer":
			queryElementId = "V_D_CUST_REF_CODE";
			color = ColorCode.customer;
			nameField = "BK_CUST_FULL_NM";
			break;

		case "LOB":
			color = ColorCode.lob;
			queryElementId = "BK_ACCT_LOB_CD";
			nameField = "BK_ACCT_LOB_CD";
			break;

		case "CountryOfRisk":
			color = ColorCode.country;
			queryElementId = "BK_CTRY_OF_RISK_ISO_CD";
			nameField = "BK_CTRY_OF_RISK_ISO_CD";
			break;

		case "LegalEntity":
			if (typeHierarchy.equals("Risk")) {
				color = ColorCode.riskLegalEntity;
				queryElementId = "BK_RSK_REF_ID";
				nameField = "BKRSKLENM";
				typeElement = "RiskLegalEntity";

			} else {
				color = ColorCode.legalEntity;
				queryElementId = "BK_LE_CD";
				nameField = "BK_LE_LDSC_TX";
			}

			break;

		case "Account":
			color = ColorCode.account;
			queryElementId = "V_ACCOUNT_NUMBER";
			nameField = "BK_ACCT_NM";
			break;

		case "InvolvedParty":
			color = ColorCode.involvedParty;
			queryElementId = "BK_INVLV_PRTY_ID";
			nameField = "BK_INVLV_PRTY_NM";
			break;

		case "RiskLegalEntity":
			color = ColorCode.riskLegalEntity;
			queryElementId = "BK_RSK_REF_ID";
			nameField = "BKRSKLENM";
			break;

		default:
			break;

		}
		List<String> hierarchyQueries = new LinkedList<>();
		String neoQueryOutgoing = "match (n:" + typeElement + "{" + queryElementId + ":" + "\"" + id + "\"})-[r:"
				+ String.valueOf(RelTypes.HIERARCHY) + "]->(p) where r.START_DATE<=" + millisecondsDate
				+ " AND r.END_DATE>" + millisecondsDate + " return n,p";
		String neoQueryIncoming = "match (n)-[r:" + String.valueOf(RelTypes.HIERARCHY) + "]->(p:" + typeElement + "{"
				+ queryElementId + ":" + "\"" + id + "\"}" + ") where r.START_DATE<=" + millisecondsDate
				+ " AND r.END_DATE>" + millisecondsDate + " return n,p";
		hierarchyQueries.add(neoQueryIncoming);
		hierarchyQueries.add(neoQueryOutgoing);
		executeQuery(oracleRangeMap, graphFinal, neoQueryOutgoing, millisecondsDate, true, id, typeElement, elementSet,
				color, nameField);
		executeQuery(oracleRangeMap, graphFinal, neoQueryIncoming, millisecondsDate, false, id, typeElement, elementSet,
				color, nameField);
		if ((!graphFinal.getIdMapper().containsKey(id)) && oracleRangeMap != null && oracleRangeMap.size() > 0) {
			if (oracleRangeMap.containsKey(id)) {
				ElementBean errorBean = oracleRangeMap.get(id);
				errorBean.setColor(ColorCode.errorNode);
				errorBean.setType("Error Node");
				errorNodeList.add(errorBean);
			}
		}
		return graphFinal;
	}
/**
 * Searches the neo4j database for matching nodes and a level of hierarchy.
 * The result is added to the graph data structure of the application
 * @param oracleRangeMap
 * @param graphFinal
 * @param query
 * @param millisecondsDate
 * @param outgoing
 * @param queryElementId
 * @param typeElement2
 * @param elementSet
 * @param color
 * @param nameField
 */
	private void executeQuery(HashMap<String, ElementBean> oracleRangeMap, GraphElements graphFinal, String query,
			long millisecondsDate, boolean outgoing, String queryElementId, String typeElement2,
			Set<ElementBean> elementSet, String color, String nameField) {
		ElementBean queryNode = new ElementBean();
		queryNode.setId(queryElementId);
		queryNode.setType(typeElement2);
		queryNode.setColor(color);
		if (oracleRangeMap != null && oracleRangeMap.size() > 0) {
			ElementBean oracleBean = new ElementBean();
			oracleBean = oracleRangeMap.get(queryNode.getId());
			if (oracleBean != null) {
				queryNode.setExposureAmount(oracleBean.getExposureAmount());
				queryNode.setExposureDuration(oracleBean.getExposureDuration());
			}
		}
		// System.out.println("the running query: " + query);
		Iterator<Map<String, Object>> iterator = mainQueryRunMethod(query);
		if (iterator.hasNext()) {
			while (iterator.hasNext()) {
				graphFinal.addVertex(queryNode, graphFinal);
				queryNode = graphFinal.findElementbyId(queryNode.getId(), graphFinal);
				String sKey = "";
				String colorProperty = "";
				Map<String, Object> row = iterator.next();
				Set<String> hierarchySet = row.keySet();
				for (String hierarchyNodeSetKey : hierarchySet) {
					if (hierarchyNodeSetKey.equals("n") || hierarchyNodeSetKey.equals("p")) {
						Node hierarchyNode = (Node) row.get(hierarchyNodeSetKey);
						long nodeNeoId = hierarchyNode.getId();
						ElementBean currentElement = new ElementBean();
						String typeElement = "";
						String sKeyproperty = "";
						String idProperty = "";
						if (hierarchyNode.hasProperty("V_D_CUST_REF_CODE")) {
							typeElement = "Customer";
							sKeyproperty = "N_CUST_SKEY";
							idProperty = "V_D_CUST_REF_CODE";
							colorProperty = ColorCode.customer;
							nameField = "BK_CUST_FULL_NM";
						} else if (hierarchyNode.hasProperty("BK_ACCT_LOB_CD")) {
							typeElement = "LOB";
							idProperty = "BK_ACCT_LOB_CD";
							colorProperty = ColorCode.lob;
							nameField = "BK_ACCT_LOB_CD";
						} else if (hierarchyNode.hasProperty("BK_CTRY_OF_RISK_ISO_CD")) {
							typeElement = "CountryOfRisk";
							idProperty = "BK_CTRY_OF_RISK_ISO_CD";
							colorProperty = ColorCode.country;
							nameField = "BK_CTRY_OF_RISK_ISO_CD";
						} else if (hierarchyNode.hasProperty("BK_LE_CD")) {
							typeElement = "LegalEntity";
							sKeyproperty = "BK_LE_SKEY";
							idProperty = "BK_LE_CD";
							colorProperty = ColorCode.legalEntity;
							nameField = "BK_LE_LDSC_TX";
						} else if (hierarchyNode.hasProperty("BK_RSK_REF_ID")) {
							typeElement = "RiskLegalEntity";
							sKeyproperty = "BK_RSK_LE_SKEY";
							idProperty = "BK_RSK_REF_ID";
							colorProperty = ColorCode.riskLegalEntity;
							nameField = "BKRSKLENM";

						} else if (hierarchyNode.hasProperty("V_ACCOUNT_NUMBER")) {
							typeElement = "Account";
							sKeyproperty = "N_ACCT_SKEY";
							idProperty = "V_ACCOUNT_NUMBER";
							colorProperty = ColorCode.account;
							nameField = "BK_ACCT_NM";

						} else if (hierarchyNode.hasProperty("BK_INVLV_PRTY_ID")) {
							typeElement = "InvolvedParty";
							sKeyproperty = "BK_INVLV_PRTY_SKEY";
							idProperty = "BK_INVLV_PRTY_ID";
							nameField = "BK_INVLV_PRTY_NM";
							colorProperty = ColorCode.involvedParty;
						}

						if (hierarchyNodeSetKey.equals("n") && outgoing) {
							queryNode.setId((String) hierarchyNode.getProperty(idProperty));
							queryNode.setType(typeElement);
							queryNode.setColor(colorProperty);
							queryNode.setName((String) hierarchyNode.getProperty(nameField));
						}
						currentElement.setId((String) hierarchyNode.getProperty(idProperty));
						currentElement.setType(typeElement);
						currentElement.setColor(colorProperty);
						currentElement.setName((String) hierarchyNode.getProperty(nameField));
						String neoQuerySurrogate = "match (n)-[r:" + String.valueOf(RelTypes.SURROGATE)
								+ "]->(p) where r.START_DATE<=" + millisecondsDate + " AND r.END_DATE>"
								+ millisecondsDate + " AND id(n) = " + nodeNeoId + " return p";
						QueryResult<Map<String, Object>> result3 = engine.query(neoQuerySurrogate,
								Collections.<String, Object> emptyMap());
						Iterator<Map<String, Object>> iterator3 = result3.iterator();
						while (iterator3.hasNext()) {
							Map<String, Object> row2 = iterator3.next();
							Set<String> set2 = row2.keySet();
							String nameValue = "";
							for (String s2 : set2) {
								if (s2.equals("p")) {
									Node surrogateNode = (Node) row2.get(s2);
									sKey = (String) surrogateNode.getProperty(sKeyproperty);
									nameValue = (String) surrogateNode.getProperty(nameField);
									if (queryNode.getId().equals(surrogateNode.getProperty(idProperty))) {
										graphFinal.findElementbyId(queryNode.getId(), graphFinal).setsKey(sKey);
										graphFinal.findElementbyId(queryNode.getId(), graphFinal).setName(nameValue);
									} else {
										currentElement.setsKey(sKey);
										currentElement.setName(nameValue);
									}
								}
							}
						}
						graphFinal.addVertex(currentElement, graphFinal);
						elementSet.add(graphFinal.findElementbyId(queryElementId, graphFinal));
						if (outgoing) {
							if (!queryNode.getId().equals(currentElement.getId())) {
								graphFinal.addEdge(queryNode, currentElement, outgoing, graphFinal);
							}
						} else {
							if (!queryNode.getId().equals(currentElement.getId())) {
								graphFinal.addEdge(currentElement, queryNode, outgoing, graphFinal);
							}
						}
						elementSet.add(graphFinal.findElementbyId(currentElement.getId(), graphFinal));
					}
				}
			}
		} else if (outgoing) {
			String idField = "";
			switch (typeElement2) {
			case "Customer":
				idField = "V_D_CUST_REF_CODE";
				nameField = "BK_CUST_FULL_NM";
				break;

			case "LOB":
				idField = "BK_ACCT_LOB_CD";
				nameField = "BK_ACCT_LOB_CD";
				break;

			case "CountryOfRisk":
				idField = "BK_CTRY_OF_RISK_ISO_CD";
				nameField = "BK_CTRY_OF_RISK_ISO_CD";
				break;

			case "LegalEntity":
				idField = "BK_LE_CD";
				nameField = "BK_LE_LDSC_TX";
				break;

			case "RiskLegalEntity":
				idField = "BK_RSK_REF_ID";
				nameField = "BKRSKLENM";
				break;

			case "Account":
				idField = "V_ACCOUNT_NUMBER";
				nameField = "BK_ACCT_NM";
				break;

			case "InvolvedParty":
				idField = "BK_INVLV_PRTY_ID";
				nameField = "BK_INVLV_PRTY_NM";
				break;

			default:
				break;

			}
			String queryCheck = "MATCH (n:" + typeElement2 + ") WHERE  n." + idField + "=~ '" + queryElementId
					+ ".*'  RETURN distinct n";
			Iterator<Map<String, Object>> iterator3 = mainQueryRunMethod(queryCheck);
			if (iterator3.hasNext()) {
				while (iterator3.hasNext()) {
					Map<String, Object> row = iterator3.next();
					Set<String> hierarchySet = row.keySet();
					for (String hierarchyNodeSetKey : hierarchySet) {
						if (hierarchyNodeSetKey.equals("n") || hierarchyNodeSetKey.equals("p")) {
							Node hierarchyNode = (Node) row.get(hierarchyNodeSetKey);
							queryNode.setName((String) hierarchyNode.getProperty(nameField));
						}
					}
				}
				graphFinal.addVertex(queryNode, graphFinal);
			}
		}
	}

	private Iterator<Map<String, Object>> mainQueryRunMethod(String query) {
		QueryResult<Map<String, Object>> result = engine.query(query, Collections.<String, Object> emptyMap());
		Iterator<Map<String, Object>> iterator = result.iterator();
		return iterator;
	}
}
