package lf.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lf.service.IDemoService;
import lf.spring.annotation.LFAutowired;
import lf.spring.annotation.LFController;
import lf.spring.annotation.LFRequestMapping;
import lf.spring.annotation.LFRequestParam;


@LFController
@LFRequestMapping("/demo")
public class DemoAction {

  	@LFAutowired 
  	private IDemoService demoService;

	@LFRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @LFRequestParam("name") String name){
		String result = demoService.get(name);
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

	@LFRequestMapping("/remove")
	public void remove(HttpServletRequest req,HttpServletResponse resp,
					   @LFRequestParam("id") Integer id){
	}

}
