package com.gupaoedu.vip.spring.formework.context;

import com.gupaoedu.vip.spring.formework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPService;
import com.gupaoedu.vip.spring.formework.aop.GpAopProxy;
import com.gupaoedu.vip.spring.formework.aop.GpCglibAopProxy;
import com.gupaoedu.vip.spring.formework.aop.GpJdkDynamicAopProxy;
import com.gupaoedu.vip.spring.formework.aop.config.GpAopConfig;
import com.gupaoedu.vip.spring.formework.aop.support.GpAdvisedSupport;
import com.gupaoedu.vip.spring.formework.beans.config.GpBeanDefinition;
import com.gupaoedu.vip.spring.formework.beans.GpBeanWrapper;
import com.gupaoedu.vip.spring.formework.beans.config.GpBeanPostProcessor;
import com.gupaoedu.vip.spring.formework.beans.support.GpBeanDefinitionReader;
import com.gupaoedu.vip.spring.formework.beans.support.GpDefaultListableFactory;
import com.gupaoedu.vip.spring.formework.core.GpBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC核心容器
 */
public class GpApplicationContext extends GpDefaultListableFactory implements GpBeanFactory {
    private String[] configLocations;

    private GpBeanDefinitionReader reader;

    /**
     * 单例的IOC容器缓存
     */
    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();

    /**
     * 通用的IOC容器
     */
    private Map<String, GpBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, GpBeanWrapper>();

    @Override
    public void refresh() throws Exception {
        // 1.定位，定位配置文件
        reader = new GpBeanDefinitionReader(this.configLocations);

        // 2.加载配置文件，扫描相关的类，把他们封装成BeanDefinition
        List<GpBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3.注册，把配置信息放到容器里面(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        // 4.把不是延迟加载的类，提前初始化
        doAutowired();
    }

    // 只处理非延迟加载的情况
    private void doAutowired() {
        for (Map.Entry<String, GpBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void doRegisterBeanDefinition(List<GpBeanDefinition> beanDefinitions) throws Exception {
        for (GpBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }

            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    public GpApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 依赖注入从这里开始，通过读取BeanDefinition中的信息
     * 然后，通过反射创建一个实例并返回
     * Spring做法是会用一个BeanWrapper来做封装，而不是返回原生对象
     * 装饰器模式，保留原来的OOP关系
     *
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        GpBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        try {
            GpBeanPostProcessor beanPostProcessor = new GpBeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);
            if (null == instance) {
                return null;
            }
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            GpBeanWrapper gpBeanWrapper = new GpBeanWrapper(instance);


            /**
             * 解决循环依赖问题
             */
            this.factoryBeanInstanceCache.put(beanName, gpBeanWrapper);

            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            //3、注入
            populateBean(beanName, new GpBeanDefinition(), gpBeanWrapper);

            return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void populateBean(String beanName, GpBeanDefinition gpBeanDefinition, GpBeanWrapper gpBeanWrapper) {
        Object instance = gpBeanWrapper.getWrappedInstance();
        Class<?> clazz = gpBeanWrapper.getWrappedClass();
        //判断只有加了注解的类才执行依赖注入
        if (!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))) {
            return;
        }

        // 获取所有的fileds
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }

            GPAutowired annotation = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = annotation.value().trim();
            // 没加value就用类型代替
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);
            try {
                // 为什么会为null
                if (null == this.factoryBeanInstanceCache.get(autowiredBeanName)) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(GpBeanDefinition beanDefinition) {
        // 1.拿到要实例化对象的类名
        String className = beanDefinition.getBeanClassName();

        // 2.反射实例化得到一个对象
        Object instance = null;
        try {
            if (this.factoryBeanInstanceCache.containsKey(className)) {
                instance = this.factoryBeanInstanceCache.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                /**
                 * 为了实现AOP
                 */
                GpAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTarget(instance);
                config.setTargetClass(clazz);

                if (config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }

                this.factoryBeanObjectCache.put(className, instance);
                this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private GpAopProxy createProxy(GpAdvisedSupport config) {
        if (config.getTargetClass().getInterfaces().length > 0) {
            return new GpJdkDynamicAopProxy(config);
        }
        return new GpCglibAopProxy(config);
    }

    private GpAdvisedSupport instantionAopConfig(GpBeanDefinition beanDefinition) {
        GpAopConfig config = new GpAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAournd(this.reader.getConfig().getProperty("aspectAround"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new GpAdvisedSupport(config);
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    /**
     * 获取所有的BeanDefinition的Name
     *
     * @return
     */
    public String[] getBeanDefinitionNames() {
        return super.beanDefinitionMap.keySet().toArray(new String[super.beanDefinitionMap.size()]);
    }

    /**
     * 获取当前的BeanDefinition个数
     *
     * @return
     */
    public int getBeanDefinitionCount() {
        return super.beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
