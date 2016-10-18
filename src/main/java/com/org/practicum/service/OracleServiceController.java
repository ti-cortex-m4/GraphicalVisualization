package com.org.practicum.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;

import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.org.practicum.dao.OracleFactDAO;
import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.tree.GraphElements;

@Component
public class OracleServiceController {
	private ApplicationContext daoContext = new ClassPathXmlApplicationContext("applicationContextDAO.xml");
	OracleFactDAO oracleFactDAO = daoContext.getBean("oracleFactDAO", OracleFactDAO.class);
	
	/**
	 * method to generate csv
	 * 
	 * @param directoryPath
	 * @return error
	 * @throws IOException
	 * @throws ParseException
	 */

	protected String loadCSV(String directoryPath) throws IOException, ParseException {
		String error = oracleFactDAO.loadCSV(directoryPath);
		return error;
 
	}
	
	/**
	 * method to find set of elements given the element type that fit the exposure range criteria specified by user
	 * 
	 * @param elementType
	 * @param date
	 * @param minExposure
	 * @param maxExposure
	 * @param exposureType
	 * @param typeHierarchy
	 * @param number
	 * @return oracleRangeMap
	 * @throws UnexpectedInputException
	 * @throws org.springframework.batch.item.ParseException
	 * @throws Exception
	 */

	protected HashMap<String, ElementBean> searchOracle(String elementType, String date, String minExposure,
			String maxExposure, String exposureType, String typeHierarchy, int number)
					throws UnexpectedInputException, org.springframework.batch.item.ParseException, Exception {

		return oracleFactDAO.findFactDataElement(elementType, date, minExposure, maxExposure, exposureType,
				typeHierarchy, number);
	}
	
	/**
	 * method to find exposure data for elements that are returned by neo4j
	 * @param graph
	 * @param date
	 * @param minExposure
	 * @param maxExposure
	 * @param elements
	 * @return graph
	 */

	protected GraphElements searchOracle(GraphElements graph, String date, String minExposure, String maxExposure,
			Set<ElementBean> elements,String typeExposure) {

		return oracleFactDAO.findFactData(elements, graph,typeExposure);
	}

}