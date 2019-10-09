package com.gupaoedu.vip.spring.formework.beans.config;

import lombok.Data;

/**
 * 用来存储配置文件的信息，相当于保存在内存中的配置
 * @author alan
 * @date 2019/10/02
 */
@Data
public class GpBeanDefinition {
    private String beanClassName;
    private boolean lazyInit = false;
    /**
     * & + beanClassName
     */
    private String factoryBeanName;
}
