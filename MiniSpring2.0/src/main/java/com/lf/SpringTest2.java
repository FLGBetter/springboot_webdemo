package com.lf;

import com.lf.springframework.context.LFApplicationContext;

public class SpringTest2 {
	public static void main(String[] args) throws Exception {
		LFApplicationContext context = new LFApplicationContext("classpath:application.properties");
		Object bean = context.getBean("lFAction");
		Object bean2 = context.getBean("iQueryService");
		System.out.println(bean);
		System.out.println(bean2);
	}
}
