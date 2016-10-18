package com.org.practicum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.org.practicum.neo4j.ElementBean;
import com.org.practicum.tree.GraphElements;

/*
 * This class holds the variables that are needed to generate the graph. This is set in session scope.
 * 
 * @param GraphElements graphFinal: the nodes and their hierarchy as found from neo4j
 * @param Map<String, List<ElementBean>> finalSourceTargetMap: source target mapping put in json which is used by fornt end to show the graph.
 *  String parameter is the id of the source, and list of element beans denote the list of children of that source
 * @param List<ElementBean> errorNodeList: list of nodes that are found in Oracle database, but not in Neo4j
 * @param int number: the limit on the number of children for nodes that the user enters
 * 
 * */

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GlobalVariables {

	private GraphElements graphFinal = new GraphElements();
	private Map<String, List<ElementBean>> finalSourceTargetMap = new HashMap<>();
	private List<ElementBean> errorNodeList = new ArrayList<>();
	private int number; 

	public GlobalVariables() {
		graphFinal = new GraphElements();
		finalSourceTargetMap = new HashMap<>();
		errorNodeList = new ArrayList<>();
		number = 0;
	}

	public GlobalVariables(GraphElements graphFinal, Map<String, List<ElementBean>> finalSourceTargetMap,
			List<ElementBean> errorNodeList, int number) {

		this.graphFinal = graphFinal;
		this.finalSourceTargetMap = finalSourceTargetMap;
		this.errorNodeList = errorNodeList;
		this.number = number;
	}

	public GraphElements getGraphFinal() {
		return graphFinal;
	}

	public void setGraphFinal(GraphElements graphFinal) {
		this.graphFinal = graphFinal;
	}

	public Map<String, List<ElementBean>> getFinalSourceTargetMap() {
		return finalSourceTargetMap;
	}

	public void setFinalSourceTargetMap(Map<String, List<ElementBean>> finalSourceTargetMap) {
		this.finalSourceTargetMap = finalSourceTargetMap;
	}

	public List<ElementBean> getErrorNodeList() {
		return errorNodeList;
	}

	public void setErrorNodeList(List<ElementBean> errorNodeList) {
		this.errorNodeList = errorNodeList;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}
