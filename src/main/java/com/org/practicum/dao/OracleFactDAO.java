package com.org.practicum.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.tree.GraphElements;

@Component
public class OracleFactDAO {

	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;// = new JdbcTemplate();
	private NamedParameterJdbcTemplate nameParaJdbcTemp;
	private static final String commaDelimiter = ",";
	private static final String newLineSeparator = "\n";
	private static String directory;

	public String loadCSV(String directoryPath) throws IOException, ParseException {
		directory = directoryPath;
		List<String> a = this.fetchDIM();
		String e = this.columnNamesCSV(a);
		e = this.createRelationshipCSV(a);
		return e;
	}

	public NamedParameterJdbcTemplate getNameParaJdbcTemp() {
		return nameParaJdbcTemp;
	}

	public void setNameParaJdbcTemp(NamedParameterJdbcTemplate nameParaJdbcTemp) {
		this.nameParaJdbcTemp = nameParaJdbcTemp;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.nameParaJdbcTemp = new NamedParameterJdbcTemplate(dataSource);
	}
/**
 * Fetches a list of tables in the database and returns it
 * @return
 */
	private List<String> fetchDIM() {
		String sql = "select table_name from user_tables";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		List<String> dimTables = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> m = list.get(i);
			String s = (String) m.get("TABLE_NAME");
			if (s.contains("DIM")) {
				dimTables.add(s);
			}
		}

		dimTables.add("Country");

		for (int j = 0; j < dimTables.size(); j++) {
			System.out.println(dimTables.get(j));
		}
		return dimTables;
	}
/**
 * creates the query and properties for relationship csv files for creating neo4j database for representing relationships between nodes 
 * @param dimTables
 * @return
 * @throws IOException
 * @throws ParseException
 */
	private String createRelationshipCSV(List<String> dimTables) throws IOException, ParseException {
		String error = "";
		boolean isRelation = true;
		String relationQuery = "";
		boolean isSurrogate = false;
		List<String> columnNamesParent = new ArrayList<String>();
		columnNamesParent.add("PARENT");
		columnNamesParent.add("CHILD");
		columnNamesParent.add("START_DATE");
		columnNamesParent.add("END_DATE");
		ArrayList<String> lables = new ArrayList<String>();
		HashMap<String, ArrayList<String>> fileNames = new HashMap<String, ArrayList<String>>();
		
		// add the query, and file name
		lables.add(
				"SELECT bk_prnt_le_cd Parent, bk_chld_le_cd Child, Start_Date, End_Date FROM(SELECT d1.bk_prnt_le_cd,d1.bk_chld_le_cd,d1.bk_chld_le_hier_lvl_cd,to_char(d1.bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(d1.bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM dim_le_hier d1 JOIN (SELECT bk_prnt_le_cd, bk_chld_le_cd, bk_chld_le_hier_lvl_cd,bk_eff_strt_dt, bk_eff_end_dt FROM dim_le_hier WHERE bk_chld_le_cd = bk_prnt_le_cd)d2 ON d1.bk_chld_le_cd = d2.bk_chld_le_cd AND d1.bk_chld_le_hier_lvl_cd=d2.bk_chld_le_hier_lvl_cd-1 UNION SELECT bk_prnt_le_cd,bk_chld_le_cd,bk_chld_le_hier_lvl_cd,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM dim_le_hier WHERE bk_chld_le_cd=bk_prnt_le_cd AND bk_chld_le_hier_lvl_cd=1)");
		lables.add("LegalEntity");
		lables.add("LegalEntity");
		//System.out.println("List:" + String.valueOf(lables));
		ArrayList<String> a = new ArrayList<String>();
		a.addAll(lables);
		fileNames.put("LE2LE", a);
		lables.clear();
		//System.out.println("List:" + String.valueOf(fileNames.get("LE2LE")));

		lables.add(
				"SELECT bk_invlv_prty_id Parent ,v_d_cust_ref_code Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_customer");
		lables.add("InvolvedParty");
		lables.add("Customer");
		ArrayList<String> a1 = new ArrayList<String>();
		a1.addAll(lables);
		fileNames.put("IPID2Customer", a1);
		lables.clear();
		//System.out.println("List:" + String.valueOf(fileNames.get("LE2LE")));

		lables.add(
				"SELECT bk_cust_id Parent,bk_acct_id Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account");
		lables.add("Customer");
		lables.add("Account");

		ArrayList<String> a2 = new ArrayList<String>();
		a2.addAll(lables);
		fileNames.put("Customer2Account", a2);
		lables.clear();

		lables.add(
				"SELECT bk_le_cd Parent,bk_acct_id Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account");
		lables.add("LegalEntity");
		lables.add("Account");

		ArrayList<String> a3 = new ArrayList<String>();
		a3.addAll(lables);
		fileNames.put("LE2Account", a3);
		lables.clear();

		lables.add(
				"SELECT bk_acct_lob_cd Parent,bk_acct_id Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account");
		lables.add("LOB");
		lables.add("Account");

		ArrayList<String> a4 = new ArrayList<String>();
		a4.addAll(lables);
		fileNames.put("LOB2Account", a4);
		lables.clear();

		lables.add(
				"SELECT bk_immd_invlvd_prty_id Parent,bk_invlv_prty_id Child,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_invlv_prty_flat_hier");
		lables.add("InvolvedParty");
		lables.add("InvolvedParty");

		ArrayList<String> a5 = new ArrayList<String>();
		a5.addAll(lables);
		fileNames.put("IPID2IPID", a5);
		lables.clear();

		lables.add(
				"SELECT v_d_cust_ref_code Parent , n_cust_skey Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_customer");
		lables.add("Customer");
		lables.add("CustomerSurrogate");

		ArrayList<String> a6 = new ArrayList<String>();
		a6.addAll(lables);
		fileNames.put("Customer2SKey", a6);
		lables.clear();

		lables.add(
				"SELECT bk_acct_id Parent,n_acct_skey Child,to_char(d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account");
		lables.add("Account");
		lables.add("AccountSurrogate");

		ArrayList<String> a7 = new ArrayList<String>();
		a7.addAll(lables);
		fileNames.put("Account2SKey", a7);
		lables.clear();

		lables.add(
				"SELECT bk_le_cd Parent,bk_le_skey Child,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_le");
		lables.add("LegalEntity");
		lables.add("LegalEntitySurrogate");

		ArrayList<String> a8 = new ArrayList<String>();
		a8.addAll(lables);
		fileNames.put("LE2SKey", a8);
		lables.clear();

		lables.add(
				"SELECT bk_rsk_ref_id Parent,bk_rsk_le_skey Child,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_rsk_le");
		lables.add("RiskLegalEntity");
		lables.add("RiskLegalEntitySurrogate");

		ArrayList<String> a9 = new ArrayList<String>();
		a9.addAll(lables);
		fileNames.put("Risk_LE2SKey", a9);
		lables.clear();

		lables.add(
				"SELECT bk_invlv_prty_id Parent, bk_invlv_prty_skey Child,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_invlv_prty");
		lables.add("InvolvedParty");
		lables.add("InvolvedPartySurrogate");

		ArrayList<String> a10 = new ArrayList<String>();
		a10.addAll(lables);
		fileNames.put("Ipid2SKey", a10);
		lables.clear();

		lables.add(
				"SELECT bk_immd_rsk_le_ref_id Parent, bk_rsk_le_ref_id Child,to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date,to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_rsk_le_flat_hier");
		lables.add("RiskLegalEntity");
		lables.add("RiskLegalEntity");

		ArrayList<String> a11 = new ArrayList<String>();
		a11.addAll(lables);
		fileNames.put("RiskLE2RiskLE", a11);
		lables.clear();

		lables.add(
				"SELECT rl.bk_rsk_ref_id Parent,a.bk_acct_id Child,to_char(a.d_record_start_date,'DD-MON-YYYY') Start_Date,to_char(a.d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account a,bk_dim_le dl, bk_dim_rsk_le rl WHERE dl.bk_le_cd = a.bk_le_cd AND dl.bk_risk_le_id=rl.bk_rsk_le_id");
		lables.add("RiskLegalEntity");
		lables.add("Account");

		ArrayList<String> a12 = new ArrayList<String>();
		a12.addAll(lables);
		fileNames.put("RiskLE2Account", a12);
		lables.clear();

		lables.add(
				"SELECT bk_ctry_of_risk_iso_cd Parent, v_d_cust_ref_code Child, to_char(d_record_start_date,'DD-MON-YYYY') Start_Date, to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_customer");
		lables.add("CountryOfRisk");
		lables.add("Customer");

		ArrayList<String> a13 = new ArrayList<String>();
		a13.addAll(lables);
		fileNames.put("Country2Customer", a13);
		lables.clear();

		lables.add(
				"SELECT bk_ctry_of_res_iso_cd Parent, bk_rsk_le_ref_id Child, to_char(bk_eff_strt_dt,'DD-MON-YYYY') Start_Date, to_char(bk_eff_end_dt,'DD-MON-YYYY') End_Date FROM bk_dim_rsk_le_flat_hier WHERE bk_ctry_of_res_iso_cd IS NOT NULL");
		lables.add("CountryOfRisk");
		lables.add("RiskLegalEntity");

		ArrayList<String> a14 = new ArrayList<String>();
		a14.addAll(lables);
		fileNames.put("Country2RiskLegalEntity", a14);
		lables.clear();

		/*
		 * lables.add(
		 * "SELECT a.bk_acct_lob_cd Parent, a.bk_cust_id Child, to_char(d_record_start_date,'DD-MON-YYYY') Start_Date, to_char(d_record_end_date,'DD-MON-YYYY') End_Date FROM dim_account a GROUP BY a.bk_acct_lob_cd,a.bk_cust_id, to_char(d_record_start_date,'DD-MON-YYYY'), to_char(d_record_end_date,'DD-MON-YYYY')"
		 * ); lables.add("LOB"); lables.add("Customer");
		 * 
		 * ArrayList<String> a15 = new ArrayList<String>(); a15.addAll(lables);
		 * fileNames.put("LOB2Customer", a15); lables.clear();
		 */

		Set<String> keys = fileNames.keySet();
		for (String key : keys) {
			//System.out.println("key: " + key);
			List<String> innerList = new ArrayList<String>();
			innerList.addAll(fileNames.get(key));
			relationQuery = innerList.get(0);

			lables.clear();
			lables.add(innerList.get(1));
			lables.add(innerList.get(2));
			//System.out.println("labels: " + lables.toString() + "size: " + lables.size());
			error = createCSV(columnNamesParent, dimTables.get(0), key, isSurrogate, isRelation, relationQuery, "",
					lables);
		}

		return error;
	}
/**
 * creates property attributes for generating csv files for nodes of neo4j
 * @param dimTables
 * @return
 * @throws IOException
 * @throws ParseException
 */
	private String columnNamesCSV(List<String> dimTables) throws IOException, ParseException {
		String error = "";
		for (int i = 0; i < dimTables.size(); i++) {
			String sql = "select COLUMN_NAME from user_tab_columns where table_name='" + dimTables.get(i) + "'";
			List<String> columnNamesParent = new ArrayList<String>();
			List<String> lableNames = new ArrayList<String>();

			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

			List<String> columnNames = new ArrayList<String>();
			for (int j = 0; j < list.size(); j++) {
				Map<String, Object> m = list.get(j);
				String s = (String) m.get("COLUMN_NAME");
				columnNames.add(s);

			}
			boolean isRelation = false;
			String relationQuery = "";
			boolean isSurrogate = false;
			switch (dimTables.get(i)) {
			case "DIM_CUSTOMER":
				columnNamesParent.add("V_D_CUST_REF_CODE");
				columnNamesParent.add("BK_CUST_FULL_NM");
				lableNames.add("Customer");
				error = createCSV(columnNamesParent, dimTables.get(i), "CUSTOMER", isSurrogate, isRelation,
						relationQuery, "V_D_CUST_REF_CODE", lableNames);
				lableNames.clear();
				lableNames.add("CustomerSurrogate");
				isSurrogate = true;
				error = createCSV(columnNames, dimTables.get(i), dimTables.get(i) + "_Surrogate", isSurrogate,
						isRelation, relationQuery, "N_CUST_SKEY", lableNames);
				break;
			case "Country":
				relationQuery = "SELECT DISTINCT(bk_ctry_of_res_iso_cd) AS \"BK_CTRY_OF_RISK_ISO_CD\" FROM bk_dim_rsk_le WHERE bk_ctry_of_res_iso_cd IS NOT NULL UNION SELECT DISTINCT(bk_ctry_of_risk_iso_cd) FROM dim_customer WHERE bk_ctry_of_risk_iso_cd IS NOT NULL";
				columnNamesParent.clear();
				lableNames.clear();
				columnNamesParent.add("BK_CTRY_OF_RISK_ISO_CD");
				lableNames.add("CountryOfRisk");
				isSurrogate = false;
				isRelation = false;
				error = createCSV(columnNamesParent, dimTables.get(i), "CountryOfRisk", isSurrogate, isRelation,
						relationQuery, "BK_CTRY_OF_RISK_ISO_CD", lableNames);
				break;
			case "DIM_ACCOUNT":
				columnNamesParent.add("V_ACCOUNT_NUMBER");
				columnNamesParent.add("BK_ACCT_NM");
				lableNames.add("Account");
				error = createCSV(columnNamesParent, dimTables.get(i), "ACCOUNT", isSurrogate, isRelation,
						relationQuery, "V_ACCOUNT_NUMBER", lableNames);
				lableNames.clear();
				lableNames.add("AccountSurrogate");
				isSurrogate = true;
				error = createCSV(columnNames, dimTables.get(i), dimTables.get(i) + "_Surrogate", isSurrogate,
						isRelation, relationQuery, "N_ACCT_SKEY", lableNames);
				columnNamesParent.clear();
				lableNames.clear();
				columnNamesParent.add("BK_ACCT_LOB_CD");
				lableNames.add("LOB");
				isSurrogate = false;
				//System.out.println("Now clearing" + columnNamesParent.toString() + lableNames.toString());
				relationQuery = "SELECT DISTINCT(bk_acct_lob_cd) FROM dim_account";
				error = createCSV(columnNamesParent, dimTables.get(i), "LOB", isSurrogate, isRelation, relationQuery,
						"BK_ACCT_LOB_CD", lableNames);
				relationQuery = "";
				break;

			case "BK_DIM_LE":
				columnNamesParent.add("BK_LE_CD");
				columnNamesParent.add("BK_LE_LDSC_TX");
				lableNames.add("LegalEntity");
				error = createCSV(columnNamesParent, dimTables.get(i), "LEGAL_ENTITY", isSurrogate, isRelation,
						relationQuery, "BK_LE_CD", lableNames);
				lableNames.clear();
				lableNames.add("LegalEntitySurrogate");
				isSurrogate = true;
				error = createCSV(columnNames, dimTables.get(i), dimTables.get(i) + "_Surrogate", isSurrogate,
						isRelation, relationQuery, "BK_LE_SKEY", lableNames);
				break;
			case "BK_DIM_RSK_LE":

				lableNames.add("RiskLegalEntity");
				columnNamesParent.add("BK_RSK_REF_ID");
				columnNamesParent.add("BKRSKLENM");
				error = createCSV(columnNamesParent, dimTables.get(i), "RISK_LEGAL_ENTITY", isSurrogate, isRelation,
						relationQuery, "BK_RSK_REF_ID", lableNames);
				lableNames.clear();
				lableNames.add("RiskLegalEntitySurrogate");
				isSurrogate = true;
				error = createCSV(columnNames, dimTables.get(i), dimTables.get(i) + "_Surrogate", isSurrogate,
						isRelation, relationQuery, "BK_RSK_LE_SKEY", lableNames);

				break;

			case "BK_DIM_INVLV_PRTY":
				columnNamesParent.add("BK_INVLV_PRTY_ID");
				columnNamesParent.add("BK_INVLV_PRTY_GRP_CD");
				columnNamesParent.add("BK_INVLV_PRTY_NM");
				lableNames.add("InvolvedParty");
				error = createCSV(columnNamesParent, dimTables.get(i), "INVOLVED_PARTY", isSurrogate, isRelation,
						relationQuery, "BK_INVLV_PRTY_ID", lableNames);
				lableNames.clear();
				lableNames.add("InvolvedPartySurrogate");
				isSurrogate = true;
				error = createCSV(columnNames, dimTables.get(i), dimTables.get(i) + "_Surrogate", isSurrogate,
						isRelation, relationQuery, "BK_INVLV_PRTY_SKEY", lableNames);

				break;
			default:
				break;
			}
		}
		return error;
	}

/**
 * Generates CSV  for node and relationship for the neo4j database
 * @param columnNames
 * @param tableName
 * @param fileName
 * @param isSurrogate
 * @param isMainRelationship
 * @param relationQuery
 * @param idParam
 * @param lableNames
 * @return
 * @throws IOException
 * @throws ParseException
 */
	private String createCSV(List<String> columnNames, String tableName, String fileName, boolean isSurrogate,
			boolean isMainRelationship, String relationQuery, String idParam, List<String> lableNames)
					throws IOException
					, ParseException
	{
		String sql = relationQuery;
		@SuppressWarnings("unused")
		String error = "";
		String fileheader = "";

		if (isMainRelationship) {
			StringBuilder headerBuilder = new StringBuilder();
			headerBuilder.append(":START_ID");
			headerBuilder.append("(" + lableNames.get(0) + "),");
			headerBuilder.append(":END_ID");
			headerBuilder.append("(" + lableNames.get(1) + "),");
			headerBuilder.append("START_DATE:LONG,");
			// headerBuilder.append("START_TIME,");
			// headerBuilder.append("END_TIMESTAMP,");
			headerBuilder.append("END_DATE:LONG");

			fileheader = headerBuilder.toString();
		}

		if (!isMainRelationship) {
			StringBuilder builder = new StringBuilder();
			for (int k = 0; k < columnNames.size() - 1; k++) {
				if (columnNames.get(k).equals(idParam))
					builder.append(columnNames.get(k) + ":ID(" + lableNames.get(0) + ")");
				else
					builder.append(columnNames.get(k));
				builder.append(",");
			}
			if (columnNames.get(columnNames.size() - 1).equals(idParam))
				builder.append(columnNames.get(columnNames.size() - 1) + ":ID(" + lableNames.get(0) + ")");
			else
				builder.append(columnNames.get(columnNames.size() - 1));

			// fileheader = "";
			fileheader = builder.toString();

			if (isSurrogate) {
				sql = "Select * from " + tableName;
			} else if (!tableName.equals("Country")) {
				StringBuilder build = new StringBuilder();
				build.append("Select DISTINCT(");
				for (int k = 0; k < columnNames.size() - 1; k++) {
					build.append(columnNames.get(k));
					if (k == 0)
						build.append("), ");
					else
						build.append(", ");
				}
				if (columnNames.size() == 1)
					build.append(columnNames.get(columnNames.size() - 1) + ")");
				else
					build.append(columnNames.get(columnNames.size() - 1));
				build.append(" from " + tableName);
				sql = build.toString();

			}
			System.out.println("sql:" + sql);

		}

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

		FileWriter fileWriter = null;
		String completeName = directory + File.separator + fileName + ".csv";
		fileWriter = new FileWriter(completeName);
		// Write the CSV file header
		fileWriter.append(fileheader);
		// Add a new line separator after the header
		fileWriter.append(newLineSeparator);
		// System.out.println(tableName + "list" + list.size());
		// Write a new student object list to the CSV file
		for (int j = 0; j < list.size(); j++) {
			Map<String, Object> m = list.get(j);
			for (String cn : columnNames) {
				Object obj = (Object) m.get(cn);

				String s = "";

				if (obj != null) {
					s = String.valueOf(obj);
				}
						String x = s;
				if (s.contains(",")) {
					x = s.replace(',', '_');
				}

				if (cn.equals("BK_CTRY_OF_RISK_ISO_CD"))
					System.out.println("value of x: " + x);

				if (cn.equals("BK_RSK_REF_ID")
						|| ((cn.equals("PARENT") && lableNames.get(0).equals("RiskLegalEntity")) || (isMainRelationship
								&& (cn.equals("CHILD") && lableNames.get(1).equals("RiskLegalEntity"))))) {

					StringBuilder zero = new StringBuilder();
					for (int i = 13 - (x.length()); i > 0; i--) {
						zero.append("0");
					}

					zero.append(x);
					x = zero.toString();
				}

				if (cn.equals("START_DATE") || cn.equals("END_DATE")) {
					long millisecondsDate = 0;
					if (x != null && x.trim().length() > 0) {
						Date myDate = new SimpleDateFormat("dd-MMM-yyyy").parse(x);
						SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
						String newDate = f.format(myDate);
						millisecondsDate = f.parse(newDate).getTime();
					}

					fileWriter.append(String.valueOf(millisecondsDate));

				} else
					fileWriter.append(x.trim());

				fileWriter.append(commaDelimiter);

			}
			fileWriter.append(newLineSeparator);

		}

		fileWriter.flush();
		fileWriter.close();
		return completeName;

	}
/**
 * Use case 1 and use case 2 : updates graph with exposure data of elements 
 * @param set
 * @param graph
 * @return
 */
	public GraphElements findFactData(Set<ElementBean> set, GraphElements graph, String typeExposure) {

		for (ElementBean element : set) {
			String skey = element.getsKey();
			String type = element.getType();
			String id = element.getId();
			String exposureAmountField = "";
			String exposureDurationField = "";
			String skeyField = "";
			String tableName = "";
			if (type.equalsIgnoreCase("LOB") || (skey != null && skey.length() > 0)) {
				switch (type) {
				case "LegalEntity":
					continue;
				case "RiskLegalEntity":
					if (typeExposure.equalsIgnoreCase("short")) {
						tableName = "bk_fct_risk_le_xpos_peak_sum_S";
					} else {
						tableName = "bk_fct_risk_le_xpos_peak_sum";
					}
					skeyField = "BK_RSK_LE_SKEY";
					exposureAmountField = "BK_MAX_XPOS_RPT_AM";
					exposureDurationField = "BK_MAX_BAL_DUR_MNTE_CT";
					break;

				case "LOB":
					tableName = "bk_fct_lob_xpos_peak";
					skeyField = "BK_LOB_SKEY";
					exposureAmountField = "BK_MAX_XPOS_RPT_AM";
					exposureDurationField = "BK_MAX_BAL_DUR_MNTE_CT";
					break;
				case "InvolvedParty":
					tableName = "bk_fct_invlv_prty_xpos_pk_sum";
					skeyField = "BK_INVLV_PRTY_SKEY";
					exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
					exposureDurationField = "BK_MAX_XPOS_DUR_MNTE_CT";
					break;
				case "Account":
					tableName = "bk_acct_fct_xpos_peak_sum";
					skeyField = "N_ACCT_SKEY";
					exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
					exposureDurationField = "BK_MAX_XPOS_DUR_MNTE_CT";
					break;
				case "Customer":
					tableName = "bk_fct_cust_xpos_peak_sum";
					skeyField = "N_CUST_SKEY";
					exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
					exposureDurationField = "BK_MAX_XPOS_DUR_MNTE_CT";
					break;
				}

				String sql = "";
				if (!type.equals("LegalEntity") && !type.equals("LOB"))
					sql = "Select * from " + tableName + " where " + skeyField + " = " + skey;
				else if (type.equals("LOB"))
					sql = "Select " + exposureAmountField + ", " + exposureDurationField + " from " + tableName
							+ " where BK_LOB_CD = '" + id + "'";
				if (sql != null && sql.length() > 0) {
					SqlRowSet row = jdbcTemplate.queryForRowSet(sql);
		
					while (row.next()) {
						element.setExposureAmount(row.getString(exposureAmountField));
						element.setExposureDuration(row.getString(exposureDurationField));
		
					}
				}
			}
		}
		return graph;

	}
/**
 * use case 3 : given the exposure range returns the set of elements that satisfy it
 * @param elementType
 * @param sdate
 * @param minimumRange
 * @param maximumRange
 * @param typeExposure
 * @param typeHierarchy
 * @param number
 * @return
 * @throws UnexpectedInputException
 * @throws org.springframework.batch.item.ParseException
 * @throws Exception
 */
	public HashMap<String, ElementBean> findFactDataElement(String elementType, String sdate, String minimumRange,
			String maximumRange, String typeExposure, String typeHierarchy, int number)
					throws UnexpectedInputException, org.springframework.batch.item.ParseException, Exception {
		HashMap<String, ElementBean> map = new HashMap<String, ElementBean>();
		//System.out.println("ranges: " + minimumRange + " " + maximumRange);
		String tableName = "";
		double minRange = 0.0;
		double maxRange = 0.0;

		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Date dDate = new Date();

		dDate = format.parse(sdate);

		java.sql.Date sqldate = new java.sql.Date(dDate.getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		String date = sdf.format(sqldate);

		String idField = "";
		String skeyField = "";
		String exposureAmountField = "";
		String exposureDurationField = "";

		switch (elementType) {
		case "LegalEntity":
			if (typeHierarchy.equals("Standard"))
				break;
			else if (typeHierarchy.equalsIgnoreCase("Risk"))
				elementType = "RiskLegalEntity";
			idField = "BK_RSK_REF_ID";
			skeyField = "BK_RSK_LE_SKEY";
			exposureAmountField = "BK_MAX_XPOS_RPT_AM";
			exposureDurationField = "BK_MAX_BAL_DUR_MNTE_CT";
			if (typeExposure.equals("short")) {
				tableName = "bk_fct_risk_le_xpos_peak_sum_S";
			} else {
				tableName = "bk_fct_risk_le_xpos_peak_sum";
			}
			break;
		case "LOB":
			tableName = "bk_fct_lob_xpos_peak";
			idField = "BK_LOB_CD";
			skeyField = "BK_LOB_SKEY";
			exposureAmountField = "BK_MAX_XPOS_RPT_AM";
			exposureDurationField = "BK_MAX_BAL_DUR_MNTE_CT";
			break;
		case "InvolvedParty":
			tableName = "bk_fct_invlv_prty_xpos_pk_sum";
			idField = "BK_INVLV_PRTY_ID";
			skeyField = "BK_INVLV_PRTY_SKEY";
			exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
			exposureDurationField = "BK_MAX_XPOS_DUR_MNTE_CT";
			break;
		case "Account":
			tableName = "bk_acct_fct_xpos_peak_sum";
			idField = "BK_ACCT_ID";
			skeyField = "N_ACCT_SKEY";
			exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
			exposureDurationField = "BK_MAX_XPOS_DUR_MINUTES_CT";
			break;
		case "Customer":
			tableName = "bk_fct_cust_xpos_peak_sum";
			idField = "BK_CUST_ID";
			skeyField = "N_CUST_SKEY";
			exposureAmountField = "BK_MAX_XPOS_RPT_CCY_AM";
			exposureDurationField = "BK_MAX_XPOS_DUR_MNTE_CT";
			break;
		}

		String sql = "";
		if ((null != minimumRange && null != maximumRange)
				&& (!minimumRange.equals("null") && !maximumRange.equals("null"))
				&& (minimumRange.trim().length() > 0 && maximumRange.trim().length() > 0)) {
			minRange = 0 - Double.parseDouble(minimumRange);
			maxRange = 0 - Double.parseDouble(maximumRange);

			if (!elementType.equals("LegalEntity")) {

				if (number != 0)

					sql = "SELECT *  FROM (SELECT * FROM " + tableName + " where (" + exposureAmountField + " between "
							+ maxRange + " and " + minRange
							+ ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy') ORDER BY " + exposureAmountField + " ASC) WHERE ROWNUM <= "
							+ number;
				else
					sql = "SELECT * FROM " + tableName + " where (" + exposureAmountField + " between " + maxRange
							+ " and " + minRange
							+ ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy')";
			}
		} else if (null != minimumRange && minimumRange.trim().length() > 0 && !minimumRange.equals("null")) {
			minRange = 0 - Double.parseDouble(minimumRange);

			if (!elementType.equals("LegalEntity")) {

				if (number != 0)

					sql = "SELECT *  FROM (SELECT * FROM " + tableName + " where (" + exposureAmountField + " <= "
							+ minRange + ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy') ORDER BY " + exposureAmountField + " ASC) WHERE ROWNUM <= "
							+ number;
				else
					sql = "SELECT * FROM " + tableName + " where (" + exposureAmountField + " <= " + minRange
							+ ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy')";
			} /*
				 * else if (elementType.equals("InvolvedParty") ||
				 * elementType.equals("Customer") ||
				 * elementType.equals("Account")) { sql = "Select * from " +
				 * tableName + " where (BK_MAX_XPOS_RPT_CCY_AM <= " + minRange +
				 * ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('"
				 * + date +
				 * "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
				 * + date + "','dd-MON-yyyy')"; }
				 */
		} else if (null != maximumRange && maximumRange.trim().length() > 0 && !maximumRange.equals("null")) {
			maxRange = 0 - Double.parseDouble(maximumRange);

			if (!elementType.equals("LegalEntity")) {

				if (number != 0)
					sql = "SELECT *  FROM (SELECT * FROM " + tableName + " where (" + exposureAmountField + " >= "
							+ maxRange + ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy') ORDER BY " + exposureAmountField + " ASC) WHERE ROWNUM <= "
							+ number;
				else
					sql = "SELECT * FROM " + tableName + " where (" + exposureAmountField + " >= " + maxRange
							+ ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('" + date
							+ "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
							+ date + "','dd-MON-yyyy')";
			} /*
				 * else if (elementType.equals("InvolvedParty") ||
				 * elementType.equals("Customer") ||
				 * elementType.equals("Account")) { sql = "Select * from " +
				 * tableName + " where (BK_MAX_XPOS_RPT_CCY_AM >= " + maxRange +
				 * ") and to_date(to_char(BK_MAX_XPOS_STRT_TS,'dd-MON-yyyy')) <= to_date('"
				 * + date +
				 * "','dd-MON-yyyy') and to_date(to_char(BK_MAX_XPOS_END_TS,'dd-MON-yyyy')) >= to_date('"
				 * + date + "','dd-MON-yyyy')"; }
				 */
		}
		System.out.println("the running oracle query; " + sql);
		if (sql != null && sql.length() > 0) {
			JdbcCursorItemReader<Object> itemReader = new JdbcCursorItemReader<Object>();
			itemReader.setDataSource(dataSource);
			itemReader.setSql(sql);
			itemReader.setRowMapper(
					new ElementRowMapper(idField, skeyField, exposureAmountField, exposureDurationField, elementType));

			ExecutionContext executionContext = new ExecutionContext();
			itemReader.open(executionContext);
			Object element = new Object();
			while (element != null) {
				element = itemReader.read();
				// counter++;
				if (element != null)
					map.put(((ElementBean) element).getId(), (ElementBean) element);
			}
			itemReader.close();
		}

		// if (sql != null && sql.length() > 0) {
		// SqlRowSet row = jdbcTemplate.queryForRowSet(sql);
		// System.out.println("row aa gayi");
		// while (row.next()) {
		// ElementBean bean = new ElementBean();
		// if (elementType.equals("RiskLegalEntity")) {
		// System.out.println("getting element id" +
		// row.getString("BK_RSK_REF_ID"));
		// bean.setId(row.getString("BK_RSK_REF_ID"));
		// System.out.println("getting le skey" +
		// row.getString("BK_RSK_LE_SKEY"));
		// bean.setsKey(row.getString("BK_RSK_LE_SKEY"));
		// System.out.println("getting xpos amount");
		// bean.setExposureAmount(row.getString("BK_MAX_XPOS_RPT_AM"));
		// System.out.println("getting duration");
		// bean.setExposureDuration(row.getString("BK_MAX_BAL_DUR_MNTE_CT"));
		// bean.setType("LegalEntity");
		// } else if (elementType.equals("LOB")) {
		// System.out.println("getting element id" +
		// row.getString("BK_LOB_CD"));
		// System.out.println("getting xpos amount" +
		// row.getString("BK_MAX_XPOS_RPT_AM"));
		//
		// bean.setId(row.getString("BK_LOB_CD"));
		// bean.setsKey(row.getString("BK_LOB_SKEY"));
		// bean.setType("LOB");
		// bean.setExposureAmount(row.getString("BK_MAX_XPOS_RPT_AM"));
		// bean.setExposureDuration(row.getString("BK_MAX_BAL_DUR_MNTE_CT"));
		// } else if (elementType.equals("InvolvedParty")) {
		// System.out.println("getting element id" +
		// row.getString("BK_INVLV_PRTY_ID"));
		// System.out.println("getting xpos amount" +
		// row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		//
		// bean.setId(row.getString("BK_INVLV_PRTY_ID"));
		// bean.setsKey(row.getString("BK_INVLV_PRTY_SKEY"));
		// bean.setType("InvolvedParty");
		// bean.setExposureAmount(row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		// bean.setExposureDuration(row.getString("BK_MAX_XPOS_DUR_MNTE_CT"));
		// } else if (elementType.equals("Account")) {
		// System.out.println("getting element id" +
		// row.getString("BK_ACCT_ID"));
		// System.out.println("getting xpos amount" +
		// row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		//
		// bean.setId(row.getString("BK_ACCT_ID"));
		// bean.setsKey(row.getString("N_ACCT_SKEY"));
		// bean.setType("Account");
		// bean.setExposureAmount(row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		// bean.setExposureDuration(row.getString("BK_MAX_XPOS_DUR_MINUTES_CT"));
		// } else if (elementType.equals("Customer")) {
		// System.out.println("getting element id" +
		// row.getString("BK_CUST_ID"));
		// System.out.println("getting xpos amount" +
		// row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		//
		// bean.setId(row.getString("BK_CUST_ID"));
		// bean.setsKey(row.getString("BK_RPT_CCY_SKEY"));
		// bean.setType("Customer");
		// bean.setExposureAmount(row.getString("BK_MAX_XPOS_RPT_CCY_AM"));
		// bean.setExposureDuration(row.getString("BK_MAX_XPOS_DUR_MNTE_CT"));
		// }
		// map.put(bean.getId(), bean);
		// }
		// }
		return map;
	}

	class ElementRowMapper implements RowMapper<Object> {

		private String id = "";
		private String skey = "";
		private String exposureAmount = "";
		private String exposureDuration = "";
		private String type = "";

		public ElementRowMapper(String idField, String skeyField, String exposureAmountField,
				String exposureDurationField, String type) {

			this.id = idField;
			this.skey = skeyField;
			this.exposureAmount = exposureAmountField;
			this.exposureDuration = exposureDurationField;
			this.type = type;
		}

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ElementBean element = new ElementBean();

			element.setId(rs.getString(id));
			element.setsKey(rs.getString(skey));
			element.setExposureAmount(rs.getString(exposureAmount));
			element.setExposureDuration(rs.getString(exposureDuration));
			element.setType(type);

			/*System.out.println("Element details: " + element.getType() + " " + element.getId() + " " + element.getsKey()
					+ " " + element.getExposureAmount() + " " + element.getExposureDuration());*/
			return element;
		}

	}
}