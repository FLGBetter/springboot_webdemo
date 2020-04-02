package com.lf.demo.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lf.demo.service.IQueryService;
import com.lf.springframework.annotation.LFService;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询业务
 *
 */
@LFService
@Slf4j
public class QueryServiceImpl implements IQueryService {

	/**
	 * 查询
	 */
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
		log.info("这是在业务方法中打印的：" + json);
		return json;
	}
	
}

