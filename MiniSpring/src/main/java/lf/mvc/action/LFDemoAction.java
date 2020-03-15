package lf.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lf.service.IDemoService;
import lf.service.impl.DemoService;
import lf.service.impl.FLService;
import lf.spring.annotation.LFAutowired;
import lf.spring.annotation.LFController;
import lf.spring.annotation.LFRequestMapping;
import lf.spring.annotation.LFRequestParam;

@LFController
@LFRequestMapping("/lf")
public class LFDemoAction {
	
	@LFAutowired 
  	private IDemoService demoService;
	
	@LFAutowired("mySerevice")
  	private FLService flService;

	@LFRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @LFRequestParam("name") String name){
		String result = flService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@LFRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@LFRequestParam("a") Integer a, @LFRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@LFRequestMapping("/getValue")
	public String get(@LFRequestParam("id") Integer id){
		return id+"";
	}
}
