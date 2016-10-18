package com.org.practicum.neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Color Code value for graph representation of elements 
@Component
public class ColorCode {

	public static final String legalEntity = "#39B5E3";
	public static final String country = "#520029";
	public static final String lob = "#E46D2D";
	public static final String customer = "#FC6793";
	public static final String account = "#87C17F";
	public static final String involvedParty = "#FFBF00";
	public static final String riskLegalEntity = "#4156F7";
	public static final String errorNode = "#BDBDBD";

	@Autowired
	public ColorCode() {
		super();
	}

}
