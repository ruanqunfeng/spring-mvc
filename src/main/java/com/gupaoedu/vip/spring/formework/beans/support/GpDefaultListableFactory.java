package com.gupaoedu.vip.spring.formework.beans.support;

import com.gupaoedu.vip.spring.formework.beans.config.GpBeanDefinition;
import com.gupaoedu.vip.spring.formework.context.support.GpAbstractApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存Bean信息
 * @author alan
 * @date 2019/10/02
 */
public class GpDefaultListableFactory extends GpAbstractApplicationContext {
    /**
     * 存储注册信息的BeanDefinition
     */
    protected final Map<String, GpBeanDefinition> beanDefinitionMap = new HashMap<String, GpBeanDefinition>();
}
