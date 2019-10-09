package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 保存url对应的函数及对象
 * @author alan
 * @date 2019/10/04
 */
@Data
public class GpHandlerMapping {
    private Object controller;
    private Method method;
    // url的封装
    private Pattern pattern;

    public GpHandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }


}
