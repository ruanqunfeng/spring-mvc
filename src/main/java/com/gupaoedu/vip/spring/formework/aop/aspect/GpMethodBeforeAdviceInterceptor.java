package com.gupaoedu.vip.spring.formework.aop.aspect;

import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInvocation;
import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInterceptor;
import java.lang.reflect.Method;

public class GpMethodBeforeAdviceInterceptor extends GpAbstractAspectAdvice implements GpAdvice,GpMethodInterceptor {
    private GpJoinPoint joinPoint;

    public GpMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method, Object[] args, Object target) throws Throwable {
        //传送了给植入参数
        super.invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(GpMethodInvocation mi) throws Throwable {
        this.joinPoint = mi;
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
