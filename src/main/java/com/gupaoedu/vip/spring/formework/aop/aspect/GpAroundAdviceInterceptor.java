package com.gupaoedu.vip.spring.formework.aop.aspect;


import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInterceptor;
import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInvocation;

import java.lang.reflect.Method;

public class GpAroundAdviceInterceptor extends GpAbstractAspectAdvice implements GpAdvice, GpMethodInterceptor {

    public GpAroundAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void around(Method method, Object[] args, Object target) throws Throwable {
        //传送了给植入参数
        super.invokeAdviceMethod(null, null, null);
    }

    @Override
    public Object invoke(GpMethodInvocation mi) throws Throwable {
        around(null,null,null);
        Object retValue = mi.proceed();
        //this.around(mi.getMethod(), mi.getArguments(), mi.getThis());
        around(null,null,null);
        return retValue;
    }
}
