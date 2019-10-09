package com.gupaoedu.vip.spring.formework.aop.config;

import lombok.Data;

/**
 * AOP配置
 * @author alan
 * @date 2019/10/08
 */
@Data
public class GpAopConfig {

    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}
