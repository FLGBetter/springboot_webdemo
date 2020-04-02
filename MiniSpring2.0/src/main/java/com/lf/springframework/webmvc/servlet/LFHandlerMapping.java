package com.lf.springframework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class LFHandlerMapping {
	
	protected Object controller;	//保存方法对应的实例
	protected Method method;		//保存映射的方法
	protected Pattern pattern;		//正则匹配路径
	
	public LFHandlerMapping(Pattern pattern,Object controller, Method method) {
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
	}
	
	
	
}
