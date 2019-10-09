package com.gupaoedu.vip.spring.demo.action;

import com.gupaoedu.vip.spring.demo.service.IModifyService;
import com.gupaoedu.vip.spring.demo.service.IQueryService;
import com.gupaoedu.vip.spring.formework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestParam;
import com.gupaoedu.vip.spring.formework.webmvc.servlet.GpModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@GPController
@GPRequestMapping("/web")
public class MyAction {

	@GPAutowired
    IQueryService queryService;
	@GPAutowired
    IModifyService modifyService;

	@GPRequestMapping("/query.json")
	public GpModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@GPRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@GPRequestMapping("/add*.json")
	public GpModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @GPRequestParam("name") String name, @GPRequestParam("addr") String addr){
		String result = null;
		try {
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail",e.getCause().getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			return new GpModelAndView("500",model);
		}

	}
	
	@GPRequestMapping("/remove.json")
	public GpModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @GPRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@GPRequestMapping("/edit.json")
	public GpModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @GPRequestParam("id") Integer id,
                               @GPRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private GpModelAndView out(HttpServletResponse resp, String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
