package com.org.practicum.neo4j;

// Bean to store data in a format for JSON required to a create D3 graph
public class SourceTarget {

	private ElementBean source;
	private ElementBean target;

	public ElementBean getSource() {
		return source;
	}

	public void setSource(ElementBean source) {
		this.source = source;
	}

	public ElementBean getTarget() {
		return target;
	}

	public void setTarget(ElementBean target) {
		this.target = target;
	}

}
