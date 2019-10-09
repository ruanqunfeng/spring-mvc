package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

public class GpViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFX = ".html";

    private File templateRootDir;
    public GpViewResolver(File templateRootDir) {
        this.templateRootDir = templateRootDir;
    }

    public GpView resolveViewName(String viewName, Locale locale) throws Exception{
        if (null == viewName || "".equals(viewName)) { return null; }

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFX);
        String path = (templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/");
        File file = new File(path);
        return new GpView(file);
    }
}
