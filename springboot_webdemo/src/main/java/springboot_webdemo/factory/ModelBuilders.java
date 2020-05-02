package springboot_webdemo.factory;

import org.springframework.stereotype.Component;

public class ModelBuilders {
	
	public static Object bulid(Class clz) throws Exception, IllegalAccessException{
		
		Object instance = clz.newInstance();
		return instance;
	}
	
}
