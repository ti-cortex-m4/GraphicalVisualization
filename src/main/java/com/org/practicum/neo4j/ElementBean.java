package com.org.practicum.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Bean representing the elements of the graph
public class ElementBean {
	String name;
	String sKey;
	String id;
	String type;
	String exposureAmount;
	String exposureDuration;
	Map<String, ElementBean> children = new HashMap<>();
	String color;

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, ElementBean> getAllChildren() {
		return children;
	}

	public ElementBean getChildById(String id) {
		return children.get(id);
	}

	public void setAllChildren(Map<String, ElementBean> children) {
		this.children = children;
	}

	public void setChildren(List<ElementBean> children) {
		for (ElementBean child : children) {
			this.children.put(child.getId(), child);
		}
	}

	public void setChild(ElementBean child) {
		// System.out.println("Setter child id"+child.getId());
		this.children.put(child.getId(), child);
	}

	public String getsKey() {
		return sKey;
	}

	public void setsKey(String sKey) {
		this.sKey = sKey;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExposureAmount() {
		return exposureAmount;
	}

	public void setExposureAmount(String exposureAmount) {
		this.exposureAmount = exposureAmount;
	}

	public String getExposureDuration() {
		return exposureDuration;
	}

	public void setExposureDuration(String exposureDuration) {
		this.exposureDuration = exposureDuration;
	}

	public ElementBean() {
		this.sKey = "";
		this.id = "";
		this.type = "";
		this.exposureAmount = "";
		this.exposureDuration = "";
		this.children = new HashMap<>();
	}

}