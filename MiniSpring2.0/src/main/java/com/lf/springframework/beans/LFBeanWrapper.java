package com.lf.springframework.beans;

public class LFBeanWrapper {
	
	private Object wrappedInstance;
//	private Class<?> wrappedClass;
	
	public LFBeanWrapper(Object wrappedInstance){
		this.wrappedInstance =wrappedInstance;
	}
	
	
	public Object  getWrappedInstance(){
		return wrappedInstance;
	}
	
	//返回代理后的Class
	// 可能会是这个 $Proxy0
	public Class<?> getWrappedClass(){
		
		return this.wrappedInstance.getClass();
	}
}
