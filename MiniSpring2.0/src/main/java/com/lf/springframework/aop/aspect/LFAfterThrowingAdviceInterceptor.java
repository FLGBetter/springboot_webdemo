package com.lf.springframework.aop.aspect;


import java.lang.reflect.Method;

import com.lf.springframework.aop.intercept.LFMethodInterceptor;
import com.lf.springframework.aop.intercept.LFMethodInvocation;

/**
 */
public class LFAfterThrowingAdviceInterceptor extends LFAbstractAspectAdvice implements LFAdvice,LFMethodInterceptor {


    private String throwingName;

    public LFAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(LFMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName){
        this.throwingName = throwName;
    }
}
