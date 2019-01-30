package com.li.myweb.supports;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.HttpContext;
import com.li.myweb.HttpRuntime;

public class CaseFilter implements Filter {
	private int contextlen;
	//private ClassLoader apploader;
	//private HttpServerUtility server;
	//private Configure appConfig;
	//public final static Pattern REGEX_UPER=Pattern.compile("[A-Z]+");
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// 如果已跳转过，直接执行
		if(request.getAttribute("CASEFILTER_FORWARD")!=null){
			chain.doFilter(request,response);
			return;
		}
		/*//设置当前的ClassLoader
		if(this.apploader!=null)
			Thread.currentThread().setContextClassLoader(this.apploader);*/
		final HttpServletRequest httpRequest=(HttpServletRequest)request;
        final HttpServletResponse httpResponse=(HttpServletResponse)response;
		//设置框架信息
		String frameInfo=(String)request.getServletContext().getAttribute(HttpRuntime.APP_DISPLAYINFO);
		if(frameInfo!=null)
			httpResponse.setHeader("E-framework",frameInfo);
		httpResponse.setHeader("Cache-Control", "max-age=0");
		//设置进入时间
		request.setAttribute(HttpRuntime.REQATTR_ENTERTIME_HTTPSERVLET, System.currentTimeMillis());
		String url=httpRequest.getRequestURI().substring(this.contextlen);
		
		boolean dispantcher=false;
		//判断是否有大写字母，如果有则转换为小写
		//boolean url2low=false;
        for(int i=0;i<url.length();i++){
			char c=url.charAt(i);
			if(c>='A'&&c<='Z'){
				dispantcher|=true;
				break;
			}
		}
        /*//判断是否包含“_action”,若包含则转换为path/action.do的形式
        String actionName=httpRequest.getParameter("_action");
        if(!(actionName==null||(actionName=actionName.trim()).length()==0)){
        	int a=url.lastIndexOf('.');
        	if(a>0){
        		url=url.substring(0,a)+"/"+actionName+url.substring(a);
        	}
        	else{
        		url=url.substring(0,a)+"/"+actionName;
        	}
        	httpRequest.setAttribute("ACTION_NAME", actionName);
        }*/
		//Matcher matcher=REGEX_UPER.matcher(url);
		//url2low=matcher.find();
        //初始化HttpContext
        //HttpContext context=new HttpContext(httpRequest,httpResponse);
        if(dispantcher){
        	url=url.toLowerCase();
        	if(url.charAt(0)!='/')
    			url="/"+url;
        	//httpResponse.setHeader("debug-context", request.getServletContext().getContextPath());
        	//httpResponse.setHeader("debug-dispatcher", url);
        	
			RequestDispatcher rd=request.getRequestDispatcher(url);
			request.setAttribute("CASEFILTER_FORWARD", true);
			rd.forward(request, response);
        }else{
        	chain.doFilter(request, response); 
        }
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		this.contextlen=arg0.getServletContext().getContextPath().length();
		/*Object objLoader=arg0.getServletContext().getAttribute("APP_CLASSLOADER");
		if(objLoader!=null)
			this.apploader=(ClassLoader)objLoader;*/
		//this.server=(HttpServerUtility)arg0.getServletContext().getAttribute(HttpRuntime.APP_ATTR_SERVER);
		//this.appConfig=(Configure)arg0.getServletContext().getAttribute(HttpRuntime.APP_CONFIGS);
	}

}
