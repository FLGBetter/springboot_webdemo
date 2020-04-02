package com.lf.springframework.beans.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lf.springframework.beans.config.LFBeanDefinition;
import com.lf.springframework.context.support.LFAbstratApplicationContext;

/**
 * 默认实现
 * @author LF
 *
 */
public class LFDefaultListableBeanFactory extends LFAbstratApplicationContext{

	
	//存储注册信息的BeanDefinition，伪Ioc容器
	public final Map<String,LFBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,LFBeanDefinition>(256);
	
	
}
