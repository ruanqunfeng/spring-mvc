package com.gupaoedu.vip.spring.formework.aop;

import com.gupaoedu.vip.spring.formework.aop.support.GpAdvisedSupport;

/**
 * Created by Tom on 2019/4/14.
 */
public class GpCglibAopProxy implements  GpAopProxy {
    public GpCglibAopProxy(GpAdvisedSupport config) {
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
