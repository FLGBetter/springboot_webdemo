package lf.service.impl;

import lf.service.IFLService;
import lf.spring.annotation.LFService;

/**
 * 核心业务逻辑
 */
@LFService("mySerevice")
public class FLService implements IFLService{

	public String get(String name) {
		return "My name is " + name;
	}

}
