package com.gupaoedu.vip.spring.formework.aop.aspect;


import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInvocation;
import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInterceptor;

import java.lang.reflect.Method;

public class GpAfterThrowingAdviceInterceptor extends GpAbstractAspectAdvice implements GpAdvice, GpMethodInterceptor {


    private String throwingName;

    public GpAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(GpMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable e) {
            invokeAdviceMethod(mi, null, e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName) {
        this.throwingName = throwName;
    }
}
