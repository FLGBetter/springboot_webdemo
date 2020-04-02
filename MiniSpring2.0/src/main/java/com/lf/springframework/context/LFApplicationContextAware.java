package com.lf.springframework.context;

/**
 * 解耦，获取IOC容器的顶层设计
 * 后面将通过一个监听器去扫描实现了该接口的类，
 * 将调用setApplicationContext方法IOC容器注入到目标类中
 * @author LF
 *
 */
public interface LFApplicationContextAware {
	
	public void setApplicationContext(LFApplicationContext applicationContext);
}
