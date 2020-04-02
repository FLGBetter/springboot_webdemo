package com.lf;

import com.lf.springframework.context.LFApplicationContext;

public class SpringTest {
	public static void main(String[] args) {
		LFApplicationContext context = new LFApplicationContext("classpath:application.properties");
		System.out.println(context);
	}
}
