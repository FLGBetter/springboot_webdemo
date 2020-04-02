package com.lf.springframework.aop.aspect;


import java.lang.reflect.Method;

import com.lf.springframework.aop.intercept.LFMethodInterceptor;
import com.lf.springframework.aop.intercept.LFMethodInvocation;

/**
 */
public class LFAfterReturningAdviceInterceptor extends LFAbstractAspectAdvice implements LFAdvice,LFMethodInterceptor {

    private LFJoinPoint joinPoint;

    public LFAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(LFMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
