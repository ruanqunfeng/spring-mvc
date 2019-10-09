package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import lombok.Data;

import java.util.Map;

@Data
public class GpModelAndView {
    private String viewName;
    private Map<String, ?> model;

    public GpModelAndView(String viewName) {
        this(viewName,null);
    }

    public GpModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }
}
