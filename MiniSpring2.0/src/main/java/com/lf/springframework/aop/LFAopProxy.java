package com.lf.springframework.aop;

/**
 */
public interface LFAopProxy {


    Object getProxy();


    Object getProxy(ClassLoader classLoader);
}
