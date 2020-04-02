package com.lf.springframework.context.support;

/**
 * IOC容器实现的顶层设计
 * @author LF
 *
 */
public abstract class LFAbstratApplicationContext {
	
	//不能new,提供子类去重写
	public void refresh(){}

}
