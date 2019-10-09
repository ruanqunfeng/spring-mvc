package com.gupaoedu.vip.spring.formework.aop.support;

import com.gupaoedu.vip.spring.formework.aop.aspect.GpAfterReturningAdviceInterceptor;
import com.gupaoedu.vip.spring.formework.aop.aspect.GpAfterThrowingAdviceInterceptor;
import com.gupaoedu.vip.spring.formework.aop.aspect.GpAroundAdviceInterceptor;
import com.gupaoedu.vip.spring.formework.aop.aspect.GpMethodBeforeAdviceInterceptor;
import com.gupaoedu.vip.spring.formework.aop.config.GpAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于生成Advise的拦截器链
 * @author alan
 * @date 2019/10/08
 */
public class GpAdvisedSupport {

    private GpAopConfig config;
    private Class<?> targetClass;
    /**
     * AOP的目标对象
     */
    private Object target;

    /**
     * expression表达式的正则
     */
    private Pattern pointCutClassPattern;

    /**
     * 存储AOP的方法通知
     */
    private transient Map<Method, List<Object>> methodCache;

    public GpAdvisedSupport(GpAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);
        if (null == cached) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());

            cached = methodCache.get(m);

            this.methodCache.put(m, cached);
        }
        return cached;
    }

    //生成Pattern表达式，用于判断是否需要实现AOP
    //解析expression表达式
    //TODO:测试 Spring中有专门的表达式，可以看一下
    private void parse() {
        //public .* com\.gupaoedu\.vip\.spring\.demo\.service\..*Service\..*\(.*\)
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        //public .* com\.gupaoedu\.vip\.spring\.demo\.service\..*Service
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        //class com\.gupaoedu\.vip\.spring\.demo\.service\..*Service
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            methodCache = new HashMap<Method, List<Object>>();
            //
            Pattern pattern = Pattern.compile(pointCut);

            Class<?> aspectClass = Class.forName(this.config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(), m);
            }

            for (Method m : this.getTargetClass().getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    //执行器链
                    List<Object> advices = new LinkedList<Object>();
                    //把每一个方法包装成 MethodInterceptor
                    //before
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        advices.add(new GpMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()), aspectClass.newInstance()));
                    }
                    if (!(null == config.getAspectAournd() || "".equals(config.getAspectAournd()))) {
                        advices.add(new GpAroundAdviceInterceptor(aspectMethods.get(config.getAspectAournd()), aspectClass.newInstance()));
                    }
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        advices.add(new GpAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()), aspectClass.newInstance()));
                    }
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        GpAfterThrowingAdviceInterceptor throwingAdvice =
                                new GpAfterThrowingAdviceInterceptor(
                                        aspectMethods.get(config.getAspectAfterThrow()),
                                        aspectClass.newInstance());
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(throwingAdvice);
                    }
                    methodCache.put(m, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTarget(Object target) {
        this.target = target;
    }


    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
