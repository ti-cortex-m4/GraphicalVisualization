package com.org.practicum.tree;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.org.practicum.service.GlobalVariables;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//class to create JSON for response
public class GSONConvertor {
	GraphElements graphFinal;

	public GSONConvertor(GraphElements graphFinal) {
		this.graphFinal = graphFinal;
	}

	public GSONConvertor() {

	}

	/**
	 * Creates and returns JSON response.
	 * 
	 * @param jsonobj
	 * @param success
	 * @param responseId
	 * @param description 
	 * @param code
	 * @param errorDescription
	 * @param globalSession 
	 * @return
	 */
	public String createJSON(Object jsonobj, boolean success, String responseId, String description, String code,
			String errorDescription, GlobalVariables globalSession) {
		Gson gson = new Gson();
		JSONParser parser = new JSONParser();
		JSONObject metadataJSONObject = new JSONObject();
		metadataJSONObject.put("success", success);
		metadataJSONObject.put("responseId", responseId);
		metadataJSONObject.put("description", description);

		JSONObject errorJSONObject = new JSONObject();
		errorJSONObject.put("code", code);
		errorJSONObject.put("description", errorDescription);
		
//		JSONObject sessionObject = new JSONObject();
//		sessionObject.put("globalSession", globalSession);

		JSONObject responseObject = new JSONObject();
		responseObject.put("metadata", metadataJSONObject);
		responseObject.put("error", errorJSONObject);
	/*	responseObject.put("globalSession", globalSession);
		System.out.println("global Session Value "+globalSession);
	*/	List<?> genericList = new ArrayList<>();
		if (jsonobj instanceof List<?>) {
			String json = gson.toJson(jsonobj);
			Object dataObject = "";
			try {
				dataObject = parser.parse(json);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			JSONArray array = (JSONArray) dataObject;
			responseObject.put("data", array);

		} else if (jsonobj instanceof String) {
			String json = (String) jsonobj;
			responseObject.put("data", json);
		}
		System.out.println("JSON object"+responseObject.toString());
		System.out.println("JSON object"+responseObject.toJSONString());
		return responseObject.toJSONString();
	}
}