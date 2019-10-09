package com.gupaoedu.vip.spring.formework.aop.intercept;

/**
 * Created by Tom on 2019/4/14.
 */
public interface GpMethodInterceptor {
    Object invoke(GpMethodInvocation invocation) throws Throwable;
}
