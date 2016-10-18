package com.org.practicum.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.neo4j.SourceTarget;
import com.org.practicum.tree.GSONConvertor;
import com.org.practicum.tree.GraphElements;

/**
 * 
 * this class is the main controller that has the request mappings, and calls
 * the appropriate methods in other classes according to the request
 *
 */
@Controller
@Scope("session")
// @RequestMapping("")
public class HeadController {
	// service controller beans
	private static ApplicationContext serviceContext;
	private static OracleServiceController oracleService;
	private static Neo4jServiceController neoService;

	// initialize the beans only once at the start
	static {
		serviceContext = new ClassPathXmlApplicationContext("applicationContextService.xml");
		oracleService = serviceContext.getBean("oracleServiceController", OracleServiceController.class);
		neoService = serviceContext.getBean("neo4jServiceController", Neo4jServiceController.class);
	}

	// this method is run when the project is run on tomcar and no specific url
	// is given.
	@RequestMapping("")
	public @ResponseBody String helloWorld() {
		Random random = new Random();
		GlobalVariables globalSession = new GlobalVariables();
		long randomNumber = (random.nextLong());
		String code = "";
		String description = "ABCD";
		String errorDescription = "";
		boolean success = true;
		String json = new GSONConvertor().createJSON("Welcome To Webxen", success, String.valueOf(randomNumber),
				description, code, errorDescription, globalSession);
		return json;
	}

	/**
	 * this method is called for auto complete for the form on the left hand
	 * side. for a particular element type, names of nodes in neo4j of that type
	 * (which satisfy the regex) are sent to front end
	 * 
	 * @param pathVars:
	 *            "type": type of element; "typeHierarchy": risk/standard; "id":
	 *            nameField on form
	 * @return response
	 */ 
	@RequestMapping(value = "/app/test/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(@RequestParam Map<String, String> pathVars) {
		GlobalVariables globalSession = new GlobalVariables();
		String typeElement = pathVars.get("type");
		String typeHierarchy = pathVars.get("typeHierarchy");
		String id = pathVars.get("id");
		String response = "";
		Random random = new Random();
		long randomNumber = (random.nextLong());
		String code = "";
		String description = "ABCD";
		String errorDescription = "";
		boolean success = true;
		if (typeElement != null && typeElement.trim().length() > 0 && !typeElement.equalsIgnoreCase("undefined"))
			response = (new GSONConvertor()).createJSON(
					neoService.fetchElementsListName(id, typeElement, typeHierarchy), success,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);

		System.out.println("response autocomplete " + response);
		return response;

	}

	/**
	 * method to generate CSV files from tables in Oracle database
	 * 
	 * @param req
	 * @return response
	 */
	@RequestMapping(value = "/app/test/generateCsv", method = RequestMethod.GET)
	public @ResponseBody String postCsvForm(HttpServletRequest req) {
		Random random = new Random();
		GlobalVariables globalSession = new GlobalVariables();
		long randomNumber = (random.nextLong());
		String code = "";
		String description = "ABCD";
		String errorDescription = "";
		boolean success = true;
		String response = "";
		String appPath = req.getServletContext().getRealPath("");
		String savePath = appPath + File.separator + "DownloadFiles";
		System.out.println("Saved Path:" + savePath);

		// A new directory will be created on server if not already present to
		// store files
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}
		System.out.println("inside generate csv: " + savePath);
 
		try {
			oracleService.loadCSV(savePath);
		} catch (IOException e) {
			e.printStackTrace();
			code = "420";
			errorDescription = "Error: Method Fialure: " + e.getMessage();
			success = false;
			response = new GSONConvertor().createJSON("Error in creating csv files. " + e.getMessage(), success,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);
			return response;
		} catch (ParseException e) {
			e.printStackTrace();
			code = "420";
			errorDescription = "Error: Method Fialure: " + e.getMessage();
			success = false;
			response = new GSONConvertor().createJSON("Error in creating csv files. " + e.getMessage(), success,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);
			return response;
		}
		return response = new GSONConvertor().createJSON("CSV Files created succesfully.", success,
				String.valueOf(randomNumber), description, code, errorDescription, globalSession);
	}

	/**
	 * this method is called when form is submitted or on double click of node
	 * 
	 * @param request
	 * @return response
	 */
	@RequestMapping(value = "/app/test/getExposure", method = RequestMethod.GET)
	public @ResponseBody String getAllProfiles(HttpServletRequest request) {

		GraphElements graphFinal = new GraphElements();
		Map<String, List<ElementBean>> finalSourceTargetMap = new HashMap<>();
		List<ElementBean> errorNodeList = new ArrayList<>();
		int number = 0;

		HttpSession session = request.getSession();
		GlobalVariables globalSession = (GlobalVariables) session.getAttribute("globalSession");
		// if the session is not null, get variables, and append results of new
		// search to older results
		if (globalSession != null) {
			graphFinal = globalSession.getGraphFinal();
			finalSourceTargetMap = globalSession.getFinalSourceTargetMap();
			errorNodeList = globalSession.getErrorNodeList();
			number = globalSession.getNumber();
		} else {
			// if session is null,initialize the graph
			globalSession = new GlobalVariables();
		}

		Random random = new Random();
		long randomNumber = (random.nextLong());
		String code = "";
		String description = "ABCD";
		String errorDescription = "";
		boolean success = true;
		String response = "";

		/*
		 * the Set of vertices element that should be sent to oracle after
		 * querying Neo4j to get exposure data
		 */
		Set<ElementBean> elementSet = new HashSet<ElementBean>();

		// get all parameters from request
		String typeElement = request.getParameter("type");
		String typeHierarchy = request.getParameter("typeHierarchy");
		String typeExposure = request.getParameter("typeExposure");
		String dt = request.getParameter("date");
		String submit = request.getParameter("submit");
		String id = request.getParameter("id");
		String minExposure = request.getParameter("minExposure");
		String maxExposure = request.getParameter("maxExposure");
		String numberString = request.getParameter("number");

		// if numberString is not given, default is all.
		if (numberString != null && numberString.trim().length() > 0 && !numberString.equalsIgnoreCase("undefined")) {
			globalSession = new GlobalVariables();
			finalSourceTargetMap = globalSession.getFinalSourceTargetMap();
			graphFinal = globalSession.getGraphFinal();
			errorNodeList = globalSession.getErrorNodeList();
			if (!numberString.equalsIgnoreCase("All")) {
				try {

					number = Integer.parseInt(numberString);

					// System.out.println("number string is not all " + number);

				} catch (NumberFormatException e2) {

					e2.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e2.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e2.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				}
			} else
				number = 0;
			// System.out.println("number string is all " + number);
			globalSession.setNumber(number);
		}

		if (submit != null && (submit.length() > 0 && submit.equalsIgnoreCase("true"))) {
			GlobalVariables globalVariables = new GlobalVariables();
			finalSourceTargetMap = globalVariables.getFinalSourceTargetMap();
			graphFinal = globalVariables.getGraphFinal();
			errorNodeList = globalVariables.getErrorNodeList();
//			number = globalVariables.getNumber();
		}

		// System.out.println("number string is" + numberString);
		/*
		 * if (numberString != null && numberString.trim().length() > 0 &&
		 * !numberString.equalsIgnoreCase("undefined")) { finalSourceTargetMap =
		 * new HashMap<>(); graphFinal = new GraphElements(); errorNodeList =
		 * new ArrayList<>(); if (!numberString.equalsIgnoreCase("All")) { try {
		 * 
		 * number = Integer.parseInt(numberString);
		 * 
		 * System.out.println("number string is not all " + number);
		 * 
		 * } catch (NumberFormatException e2) {
		 * 
		 * e2.printStackTrace(); code = "420"; errorDescription =
		 * "Error: Method Fialure: " + e2.getMessage(); success = false;
		 * response = new GSONConvertor().createJSON("Error in getting graph. "
		 * + e2.getMessage(), success, String.valueOf(randomNumber),
		 * description, code, errorDescription); return response; } } else
		 * number = 0; System.out.println("number string is all " + number);
		 * 
		 * }
		 */

		try {
			dt = dt.substring(4, 15);
		} catch (StringIndexOutOfBoundsException e2) {
			code = "420";
			errorDescription = "Error: Method Fialure: " + e2.getMessage();
			success = false;
			response = new GSONConvertor().createJSON("Error in getting graph. " + e2.getMessage(), success,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);
			return response;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
		Date d = new Date();

		if (typeHierarchy == null || typeHierarchy.equalsIgnoreCase("undefined"))
			typeHierarchy = "";
		try {
			d = sdf.parse(dt);

		} catch (ParseException e2) {
			e2.printStackTrace();
			code = "420";
			errorDescription = "Error: Method Fialure: " + e2.getMessage();
			success = false;
			response = new GSONConvertor().createJSON("Error in getting graph. " + e2.getMessage(), success,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);
			return response;
		}
		SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy");
		dt = sd.format(d);
		// String

		if (minExposure != null && minExposure.equals("undefined")) {
			minExposure = null;
		}
		if (maxExposure != null && maxExposure.equals("undefined")) {
			maxExposure = null;
		}
		if (id != null && id.equals("undefined"))
			id = null;

		System.out.println("The input parameters are " + typeElement + " " + typeExposure + " " + dt + " " + minExposure
				+ " " + maxExposure + " " + id + " hier type = " + typeHierarchy + " number string " + numberString);

		HashMap<String, ElementBean> oracleRangeMap = new HashMap<String, ElementBean>();

		if (dt == null || dt.trim().length() == 0) {
			response = new GSONConvertor().createJSON("Please provide a date to begin your search", true,
					String.valueOf(randomNumber), description, code, errorDescription, globalSession);
			return response;
		}

		// if the type of element is specified by the user in search terms
		if (typeElement != null && typeElement.trim().length() > 0) {

			if (typeElement.equalsIgnoreCase("LegalEntity") && submit.equalsIgnoreCase("true")) {
				if (typeExposure == null || typeExposure.trim().length() == 0) {
					response = new GSONConvertor().createJSON(
							"Please provide type of exposure of LE to begin your search", false,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				}
				// typeHierachy should be specified when the form is first
				// submitted
				if (submit.equalsIgnoreCase("true"))
					if (typeHierarchy == null || typeHierarchy.trim().length() == 0) {
						response = new GSONConvertor().createJSON(
								"Please provide type of hierarchy for LE to begin your search", false,
								String.valueOf(randomNumber), description, code, errorDescription, globalSession);
						return response;
					}
			}

			/* if type of element, id and date specified -- USE CASE 1 and 2 */
			if (id != null && id.trim().length() > 0) {

				if ((submit != null && submit.trim().length() > 0 && submit.equalsIgnoreCase("true")
						&& !id.equalsIgnoreCase("undefined"))
						|| (numberString != null && numberString.trim().length() > 0
								&& !numberString.equalsIgnoreCase("undefined"))) {
					id = neoService.fetchElementsList(id, typeElement, typeHierarchy).getId();
				}

				/*
				 * first get the graph form neo4j, send it to oracle so that
				 * they can append their data in the same graph, and return the
				 * updated one
				 */
				try {
					graphFinal = neoService.searchNeo(oracleRangeMap, graphFinal, typeElement, id, dt, typeHierarchy,
							elementSet, errorNodeList);

				} catch (ParseException e) {
					e.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				}
				graphFinal = oracleService.searchOracle(graphFinal, dt, minExposure, maxExposure, elementSet,typeExposure);
			} else if ((id == null || id.trim().length() == 0 || id.equalsIgnoreCase("undefined"))
					&& ((minExposure != null && minExposure.length() > 0)
							|| (maxExposure != null && maxExposure.length() > 0))) {
				/*
				 * THIRD USE CASE: if only type and range specified, call oracle
				 * first to get list of items satisfying the range criterion,
				 * query neo4j for that list, find exposure of all items in all
				 * hierarchies
				 */

				// System.out.println("in the third use case");
				globalSession = new GlobalVariables();
				graphFinal = globalSession.getGraphFinal();
				errorNodeList = globalSession.getErrorNodeList();
				finalSourceTargetMap = globalSession.getFinalSourceTargetMap();
				globalSession.setNumber(number);
				try {

					oracleRangeMap = oracleService.searchOracle(typeElement, dt, minExposure, maxExposure, typeExposure,
							typeHierarchy, number);
				} catch (ParseException e1) {
					e1.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e1.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e1.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				} catch (UnexpectedInputException e1) {
					e1.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e1.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e1.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				} catch (org.springframework.batch.item.ParseException e1) {
					e1.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e1.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e1.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				} catch (Exception e1) {
					e1.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e1.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e1.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				}
				try {
					graphFinal = neoService.searchNeo(oracleRangeMap, graphFinal, dt, typeHierarchy, elementSet,
							errorNodeList);
					graphFinal = oracleService.searchOracle(graphFinal, dt, minExposure, maxExposure, elementSet,typeExposure);
				} catch (ParseException e) {
					e.printStackTrace();
					code = "420";
					errorDescription = "Error: Method Fialure: " + e.getMessage();
					success = false;
					response = new GSONConvertor().createJSON("Error in getting graph. " + e.getMessage(), success,
							String.valueOf(randomNumber), description, code, errorDescription, globalSession);
					return response;
				}

			}
			// method to limit nodes based on number specified by user, and the
			// exposure range for use case 2 and 3
			boolean[] visited = new boolean[graphFinal.getVertices().size()];
			if (id == null || id.trim().length() == 0) {
				finalSourceTargetMap = graphFinal.traverseGraphMain(graphFinal, visited, 0, finalSourceTargetMap,
						number, minExposure, maxExposure, id);
			} else {
				finalSourceTargetMap = graphFinal.traverseGraphMain(graphFinal, visited,
						graphFinal.getIdMapper().get(id), finalSourceTargetMap, number, minExposure, maxExposure, id);
			}
			// create source target list for json according to results returned
			// after limiting
			List<SourceTarget> jsonGraph = new ArrayList<SourceTarget>();
			Set<String> mapSourceKeys = finalSourceTargetMap.keySet();
			for (String key : mapSourceKeys) {

				ElementBean source = new ElementBean();
				source = graphFinal.findElementbyId(key, graphFinal);

				List<ElementBean> targets = finalSourceTargetMap.get(key);

				for (ElementBean target : targets) {
					SourceTarget srcTarget = new SourceTarget();
					srcTarget.setSource(source);
					srcTarget.setTarget(target);
					jsonGraph.add(srcTarget);
				}

			}
			// add the error nodes
			for (ElementBean errorNode : errorNodeList) {
				SourceTarget srcTarget = new SourceTarget();
				srcTarget.setSource(errorNode);
				srcTarget.setTarget(errorNode);
				jsonGraph.add(srcTarget);
			}
			
			//set the results in session
			globalSession.setErrorNodeList(errorNodeList);
			globalSession.setFinalSourceTargetMap(finalSourceTargetMap);
			globalSession.setGraphFinal(graphFinal);
			globalSession.setNumber(number);

			session.setAttribute("globalSession", globalSession);
			// System.out.println("globalSession value" + globalSession);
			// System.out.println("globalSession value" +
			// globalSession.getGraphFinal());
			//
			// System.out.println("globalSession value" +
			// globalSession.getGraphFinal().getIdMapper().size());
			//
			// System.out.println("globalSession value" + globalSession);

			response = new GSONConvertor(graphFinal).createJSON(jsonGraph, true, String.valueOf(randomNumber),
					description, code, errorDescription, globalSession);

		} else {
			response = new GSONConvertor().createJSON("Please provide the type of element to begin your search", false,
					String.valueOf(randomNumber), description, "420", "Missing Parameter", globalSession);
			return response;
		}
		System.out.println("number string: " + numberString);

		return response;
	}
}