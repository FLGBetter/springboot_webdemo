package com.lf.demo.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lf.demo.service.IModifyService;
import com.lf.demo.service.IQueryService;
import com.lf.demo.service.impl.ModifyServiceImpl;
import com.lf.springframework.annotation.LFAutowired;
import com.lf.springframework.annotation.LFController;
import com.lf.springframework.annotation.LFRequestMapping;
import com.lf.springframework.annotation.LFRequestParam;


/**
 * 公布接口url
 *
 */
@LFController
@LFRequestMapping("/web")
public class LFAction {

	@LFAutowired 
	private IQueryService queryService;
	@LFAutowired 
	private IModifyService modifyService = new ModifyServiceImpl();

	@LFRequestMapping("/query.json")
	public void query(HttpServletRequest request, HttpServletResponse response,
								@LFRequestParam("name") String name){
		String result = queryService.query(name);
		out(response,result);
	}
	
	@LFRequestMapping("/add*.json")
	public void add(HttpServletRequest request,HttpServletResponse response,
			   @LFRequestParam("name") String name,@LFRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		out(response,result);
	}
	
	@LFRequestMapping("/remove.json")
	public void remove(HttpServletRequest request,HttpServletResponse response,
		   @LFRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		out(response,result);
	}
	
	@LFRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,
			@LFRequestParam("id") Integer id,
			@LFRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		out(response,result);
	}
	
	
	
	private void out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
