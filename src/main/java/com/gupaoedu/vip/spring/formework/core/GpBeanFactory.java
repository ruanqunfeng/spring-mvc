package com.gupaoedu.vip.spring.formework.core;

/**
 * 单例工厂的顶层设计
 * @author alan
 * @date 2019/10/02
 */
public interface GpBeanFactory {

    /**
     * 根据beanName从IOC容器中获取实例Bean
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    Object getBean(Class<?> beanClass) throws Exception;
}
