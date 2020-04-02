package com.lf.springframework.core;

public interface LFBeanFactory {
	
	//根据beanName从IOC容器取出bean
	Object getBean(String beanName) throws Exception;
}
