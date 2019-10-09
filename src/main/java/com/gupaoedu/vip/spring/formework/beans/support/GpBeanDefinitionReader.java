package com.gupaoedu.vip.spring.formework.beans.support;

import com.gupaoedu.vip.spring.formework.beans.config.GpBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 对配置文件进行查找，读取和解析
 *
 * @author alan
 * @date 2019/10/02
 */
public class GpBeanDefinitionReader {
    /**
     * 保存每个BeanName
     */
    private List<String> registyBeanClasses = new ArrayList<String>();

    /**
     * 用来读取配置文件
     */
    private Properties config = new Properties();

    private final String SCAN_PACKAGE = "scanPackage";

    public GpBeanDefinitionReader(String... locations) {
        // 通过URL定位找到其所对应的文件，然后转换为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String property) {
        //把.转换成/   com.gupaoedu.vip.spring.demo
        URL url = this.getClass().getClassLoader().getResource("/" + property.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(property + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = property + "." + file.getName().replace(".class", "");
                registyBeanClasses.add(className);
            }
        }
    }

    public Properties getConfig() {
        return config;
    }

    /**
     * 加载BeanDefinition
     * @return
     */
    public List<GpBeanDefinition> loadBeanDefinitions() {
        List<GpBeanDefinition> result = new ArrayList<GpBeanDefinition>();
        try {
            for (String className : registyBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) { continue; }
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> aClass : interfaces) {
                    result.add(doCreateBeanDefinition(aClass.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private GpBeanDefinition doCreateBeanDefinition(String factoryName, String beanClassName) {
        GpBeanDefinition beanDefinition = new GpBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
