package com.gupaoedu.vip.spring.formework.aop.aspect;


import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInterceptor;
import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInvocation;

import java.lang.reflect.Method;

public class GpAfterReturningAdviceInterceptor extends GpAbstractAspectAdvice implements GpAdvice,GpMethodInterceptor {

    private GpJoinPoint joinPoint;

    public GpAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    // TODO:retVal哪来的？
    @Override
    public Object invoke(GpMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
