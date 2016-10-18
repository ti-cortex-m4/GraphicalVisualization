package com.org.practicum.service;

import java.util.ArrayList;
import java.util.List;
 
public class User {
 
	private int id = 1;
	private String name = "Aayush";
	private List<String> messages = new ArrayList<String>() {
		{
			add("msg 1");
			add("msg 2");
			add("msg 3");
		}
	};
 
	//getter and setter methods
 
	@Override
	public String toString() {
		return "User [age=" + id + ", name=" + name + ", " +
				"messages=" + messages + "]";
	}
}