package com.org.practicum.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.org.practicum.dao.Neo4jDimDAO;
import com.org.practicum.neo4j.AutocompleteName;
import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.neo4j.ElementList;
import com.org.practicum.tree.GraphElements;

/**
 * 
 * service controller for neo4j
 *
 */
@Component
public class Neo4jServiceController {
	private ApplicationContext daoContext = new ClassPathXmlApplicationContext("applicationContextDAO.xml");
	Neo4jDimDAO neoDAO = daoContext.getBean("neo4jDimDAO", Neo4jDimDAO.class);

	/**
	 * method to map name to id of the element given the name and type of
	 * element
	 * 
	 * @param name
	 * @param typeElement
	 * @param typeHierarchy
	 * @return ElementList
	 */
	protected ElementList fetchElementsList(String name, String typeElement, String typeHierarchy) {
		return neoDAO.fetchElementsList(name, typeElement, typeHierarchy);

	}
 
	/**
	 * method for auto complete given the type of element
	 * 
	 * @param name
	 * @param typeElement
	 * @param typeHierarchy
	 * @return List<Autocomplete>
	 */
	protected List<AutocompleteName> fetchElementsListName(String name, String typeElement, String typeHierarchy) {
		return neoDAO.fetchElementsListName(name, typeElement, typeHierarchy);

	}

	/*
	 * this method is called for third use case. for every element bean in the
	 * map returned by oracle, get the first level hierarchy form neo4j
	 */
	/**
	 * 
	 * @param oracleRangeMap
	 * @param graphFinal
	 * @param date
	 * @param typeHierarchy
	 * @param elementSet
	 * @param errorNodeList
	 * @return graph
	 * @throws ParseException
	 */
	protected GraphElements searchNeo(HashMap<String, ElementBean> oracleRangeMap, GraphElements graphFinal,
			String date, String typeHierarchy, Set<ElementBean> elementSet, List<ElementBean> errorNodeList)
					throws ParseException {

		if (oracleRangeMap != null) {
			Set<String> keySet = oracleRangeMap.keySet();
			for (String elementKey : keySet) {
				graphFinal = searchNeo(oracleRangeMap, graphFinal, oracleRangeMap.get(elementKey).getType(),
						oracleRangeMap.get(elementKey).getId(), date, typeHierarchy, elementSet, errorNodeList);
			}
		}

		return graphFinal;
	}

	/*
	 * method to find the first level hierarchy of the element given the id, and
	 * type of element
	 */
	/**
	 * 
	 * @param oracleRangeMap
	 * @param graphFinal
	 * @param typeElement
	 * @param id
	 * @param date
	 * @param typeHierarchy
	 * @param elementSet
	 * @param errorNodeList
	 * @return graph
	 * @throws ParseException
	 */
	protected GraphElements searchNeo(HashMap<String, ElementBean> oracleRangeMap, GraphElements graphFinal,
			String typeElement, String id, String date, String typeHierarchy, Set<ElementBean> elementSet,
			List<ElementBean> errorNodeList) throws ParseException {
		neoDAO.searchNeo(oracleRangeMap, graphFinal, typeElement, id, date, typeHierarchy, elementSet, errorNodeList);
		return graphFinal;

	}
}