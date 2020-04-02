package com.lf.springframework.beans.config;

import lombok.Data;

@Data
public class LFBeanDefinition {

	private String beanClassName;
	private boolean lazyInit = false;
	private boolean isSingleton = true;
	private String factoryBeanName;
}
