package com.lf.springframework.aop.aspect;


import java.lang.reflect.Method;

import com.lf.springframework.aop.intercept.LFMethodInterceptor;
import com.lf.springframework.aop.intercept.LFMethodInvocation;

/**
 */
public class LFMethodBeforeAdviceInterceptor extends LFAbstractAspectAdvice implements LFAdvice,LFMethodInterceptor {


    private LFJoinPoint joinPoint;
    public LFMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method,Object[] args,Object target) throws Throwable{
        //传送了给织入参数
        //method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint,null,null);

    }
    @Override
    public Object invoke(LFMethodInvocation mi) throws Throwable {
        //从被织入的代码中才能拿到，JoinPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
