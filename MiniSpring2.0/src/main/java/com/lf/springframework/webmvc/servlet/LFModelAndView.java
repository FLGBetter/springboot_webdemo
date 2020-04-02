package com.lf.springframework.webmvc.servlet;

import java.util.Map;

public class LFModelAndView {
	
	private String viewName; 
	private Map<String,?> model;
	
	public LFModelAndView(String viewName) {
		this(viewName,null);
	}
	
	public LFModelAndView(String viewName, Map<String, ?> model) {
		this.viewName = viewName;
		this.model = model;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public Map<String, ?> getModel() {
		return model;
	}

	public void setModel(Map<String, ?> model) {
		this.model = model;
	}
	
}
