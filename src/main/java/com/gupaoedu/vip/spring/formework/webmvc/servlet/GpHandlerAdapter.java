package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import com.gupaoedu.vip.spring.formework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 把统一的http参数转换成对应的类型的参数
 *
 * @author alan
 * @date 2019/10/04
 */
public class GpHandlerAdapter {
    public boolean supports(Object handler) {
        return handler instanceof GpHandlerMapping;
    }

    public GpModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        GpHandlerMapping handlerMapping = (GpHandlerMapping) handler;

        // 保存形参列表，参数名及对应的下标
        Map<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

        // 获取命名参数
        // TODO:如果参进来的是一个类(JSON类)，要怎么转换。
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof GPRequestParam) {
                    String paramName = ((GPRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        // 获取方法中的request和response参数
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == HttpServletRequest.class
                    || parameterTypes[i] == HttpServletResponse.class) {
                paramIndexMapping.put(parameterTypes[i].getName(),i);
            }
        }

        //获得方法的形参列表
        //不包括HttpServletRequest,HttpServletResponse
        Map<String, String[]> params = req.getParameterMap();

        //实参列表
        //TODO:能否用paramIndexMapping.size()作为长度
        Object[] paramValues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> parm : params.entrySet()) {
            // TODO：作用是什么？
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s",",");

            if (!paramIndexMapping.containsKey(parm.getKey())) { continue; }

            int index = paramIndexMapping.get(parm.getKey());
            paramValues[index] = caseStringValue(value, parameterTypes[index]);
        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
        if (null == result || result instanceof Void) { return null; }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == GpModelAndView.class;
        if (isModelAndView) {
            return (GpModelAndView)result;
        }

        return null;

    }

    private Object caseStringValue(String value, Class<?> paramsType) {
        if (String.class == paramsType) {
            return value;
        }
        //如果是int
        if (Integer.class == paramsType) {
            return Integer.valueOf(value);
        } else if (Double.class == paramsType) {
            return Double.valueOf(value);
        } else {
            return value;
        }
    }
}
