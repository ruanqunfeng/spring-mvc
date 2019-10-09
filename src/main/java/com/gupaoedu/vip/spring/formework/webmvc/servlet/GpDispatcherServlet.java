package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.formework.context.GpApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring MVC的启动入口
 *
 * @author alan
 * @date 2019/10/02
 */
public class GpDispatcherServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    private List<GpHandlerMapping> handlerMappings = new ArrayList<GpHandlerMapping>();

    /**
     * Spring使用List，调用的时候循环获取
     */
    private Map<GpHandlerMapping, GpHandlerAdapter> handlerAdapters = new HashMap<GpHandlerMapping, GpHandlerAdapter>();

    private List<GpViewResolver> viewResolvers = new ArrayList<GpViewResolver>();

    private GpApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,Details:\r\n" +
                    Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "")
                            .replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1.通过req中拿到URL，去匹配一个HandlerMapping
        GpHandlerMapping handler = getHandler(req);
        if (null == handler) {
            processDispatchResult(req,resp,new GpModelAndView("404"));
            return;
        }

        // 2.准备调用前的参数
        GpHandlerAdapter ha = getHandlerAdapter(handler);
        if (null == ha) {
            processDispatchResult(req,resp,new GpModelAndView("404"));
            return;
        }

        // 3.真正的调用方法,返回ModelAndView存储了要返回页面上的值，和页面模板的名称
        GpModelAndView mv = ha.handle(req,resp,handler);

        // 这一步才是真正的输出
        processDispatchResult(req, resp, mv);


    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GpModelAndView mv) throws Exception {
        // 把得到的ModelAndView转换成一个HTML，JSON等
        if (null == mv) {
            return;
        }
        if (this.viewResolvers.isEmpty()) { return; }
        for (GpViewResolver viewResolver : this.viewResolvers) {
            GpView view = viewResolver.resolveViewName(mv.getViewName(), null);
            if (null == view) { continue; }
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private GpHandlerAdapter getHandlerAdapter(GpHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) { return null; }
        GpHandlerAdapter ha = this.handlerAdapters.get(handler);
        if (ha.supports(handler)) {
            return ha;
        }
        return null;
    }

    private GpHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) { return null; }

        String url = req.getRequestURI();
        // 返回当前站点的根目录名字
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        for (GpHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) { continue; }
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        context = new GpApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }


    //初始化策略
    protected void initStrategies(GpApplicationContext context) {
        //多文件上传的组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);


        //handlerMapping，必须实现
        initHandlerMappings(context);
        //初始化参数适配器，必须实现
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);


        //初始化视图转换器，必须实现
        initViewResolvers(context);
        //参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(GpApplicationContext context) {
    }

    private void initViewResolvers(GpApplicationContext context) {
        // 拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i < templates.length; i++) {
            this.viewResolvers.add(new GpViewResolver(templateRootDir));
        }

    }

    private void initRequestToViewNameTranslator(GpApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(GpApplicationContext context) {
    }

    private void initHandlerAdapters(GpApplicationContext context) {

        // 根据HandlerMapping生成对应的HandlerAdapter
        for (GpHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new GpHandlerAdapter());
        }
    }

    /**
     * 根据BeanDefinition把url和method做映射
     *
     * @param context
     */
    private void initHandlerMappings(GpApplicationContext context) {
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanDefinitionNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();

                if (!clazz.isAnnotationPresent(GPController.class)) {
                    continue;
                }

                String baseUrl = "";
                if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                    GPRequestMapping annotation = clazz.getAnnotation(GPRequestMapping.class);
                    baseUrl = annotation.value();
                }

                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                        continue;
                    }
                    GPRequestMapping annotation = method.getAnnotation(GPRequestMapping.class);

                    String regex = ("/" + baseUrl + "/" + annotation.value()).replaceAll("\\*", ".*")
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new GpHandlerMapping(controller, method, pattern));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initThemeResolver(GpApplicationContext context) {
    }

    private void initLocaleResolver(GpApplicationContext context) {
    }

    private void initMultipartResolver(GpApplicationContext context) {
    }

}
