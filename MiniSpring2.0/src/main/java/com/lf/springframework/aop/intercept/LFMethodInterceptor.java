package com.lf.springframework.aop.intercept;

/**
 */
public interface LFMethodInterceptor {
    Object invoke(LFMethodInvocation invocation) throws Throwable;
}
