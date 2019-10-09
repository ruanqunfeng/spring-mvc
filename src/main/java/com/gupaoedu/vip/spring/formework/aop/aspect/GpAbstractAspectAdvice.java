package com.gupaoedu.vip.spring.formework.aop.aspect;

import java.lang.reflect.Method;

public abstract class GpAbstractAspectAdvice implements GpAdvice {
    private Method aspectMethod;
    private Object aspectTarget;
    public GpAbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    public Object invokeAdviceMethod(GpJoinPoint joinPoint, Object returnValue, Throwable tx) throws Throwable{
        Class<?>[] paramTypes = this.aspectMethod.getParameterTypes();
        if (null == paramTypes || paramTypes.length == 0) {
            // 没有参数就直接调用
            return this.aspectMethod.invoke(aspectTarget);
        } else {
            // 有参数就把参数加进去
            Object [] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i ++) {
                if(paramTypes[i] == GpJoinPoint.class){
                    args[i] = joinPoint;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = tx;
                }else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget,args);
        }
    }
}
