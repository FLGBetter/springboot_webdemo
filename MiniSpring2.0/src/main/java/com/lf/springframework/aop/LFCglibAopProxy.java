package com.lf.springframework.aop;

import com.lf.springframework.aop.support.LFAdvisedSupport;

/**
 */
public class LFCglibAopProxy implements  LFAopProxy {
    public LFCglibAopProxy(LFAdvisedSupport config) {
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
