package com.gupaoedu.vip.spring.formework.aop;

import com.gupaoedu.vip.spring.formework.aop.intercept.GpMethodInvocation;
import com.gupaoedu.vip.spring.formework.aop.support.GpAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class GpJdkDynamicAopProxy implements GpAopProxy,InvocationHandler {
    private GpAdvisedSupport advised;

    public GpJdkDynamicAopProxy(GpAdvisedSupport config){
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return this.getProxy(this.advised.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method,this.advised.getTargetClass());
        GpMethodInvocation invocation = new GpMethodInvocation(proxy,method,this.advised.getTarget(),args,interceptorsAndDynamicMethodMatchers,this.advised.getTargetClass());
        return invocation.proceed();
    }
}
