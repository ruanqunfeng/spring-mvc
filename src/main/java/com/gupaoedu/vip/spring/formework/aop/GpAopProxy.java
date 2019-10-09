package com.gupaoedu.vip.spring.formework.aop;

public interface GpAopProxy {
    Object getProxy();


    Object getProxy(ClassLoader classLoader);
}
