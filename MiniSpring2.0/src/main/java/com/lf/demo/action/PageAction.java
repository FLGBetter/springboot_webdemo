package com.lf.demo.action;

import java.util.HashMap;
import java.util.Map;

import com.lf.demo.service.IQueryService;
import com.lf.demo.service.impl.QueryServiceImpl;
import com.lf.springframework.annotation.LFAutowired;
import com.lf.springframework.annotation.LFController;
import com.lf.springframework.annotation.LFRequestMapping;
import com.lf.springframework.annotation.LFRequestParam;
import com.lf.springframework.webmvc.servlet.LFModelAndView;

/*** 公布接口 url 
*/
@LFController
@LFRequestMapping("/")
public class PageAction {
	
//	@LFAutowired
	IQueryService queryService = new QueryServiceImpl();
	
	@LFRequestMapping("/first.html")
	public LFModelAndView query(@LFRequestParam("teacher") String teacher) {
		String result = queryService.query(teacher);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("teacher", teacher);
		model.put("data", result);
		model.put("token", "123456");
		return new LFModelAndView("first.html", model);
	}
}
