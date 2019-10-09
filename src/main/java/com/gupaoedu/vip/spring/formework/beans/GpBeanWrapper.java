package com.gupaoedu.vip.spring.formework.beans;

/**
 * 保存Bean的信息
 * @author alan
 * @date 2019/10/02
 */
public class GpBeanWrapper {
    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public GpBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    /**
     * 返回代理以后的class
     * 可能会是这个 $Proxy0（动态代理）
     * 获取Class对象，可以生成实例，实现多例
     * @return
     */
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
