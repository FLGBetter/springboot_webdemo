package lf.service.impl;

import lf.service.IDemoService;
import lf.spring.annotation.LFService;

/**
 * 核心业务逻辑
 */
@LFService
public class DemoService implements IDemoService{

	public String get(String name) {
		return "My name is " + name;
	}

}
