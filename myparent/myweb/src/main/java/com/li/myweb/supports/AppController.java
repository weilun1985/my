package com.li.myweb.supports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import com.li.myweb.Configure;
import com.li.myweb.HttpRuntime;
import com.li.myweb.HttpServerUtility;
import com.li.myweb.HttpApplication;
import com.li.myweb.security.*;
import com.li.myweb.caching.*;

public class AppController {
	public static void Startup(ServletContext context){
		String configString=context.getInitParameter(HttpRuntime.FRAME_CONFIG_PAREMNAME);
		Configure configure=null;
		MyContextAttributeListener scal=new MyContextAttributeListener();
		context.addListener(scal);
		try
		{
			if(context.getAttribute(HttpRuntime.APP_STARTTIME)!=null)
				return;
			//设置启动时间
			Date startTime=new Date();
			context.setAttribute(HttpRuntime.APP_STARTTIME,startTime);
			if(context.getAttribute(HttpRuntime.APP_DISPLAYINFO)==null){
				SimpleDateFormat dateformat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
				String file=AppController.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				String version=file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
				context.setAttribute(HttpRuntime.APP_DISPLAYINFO, String.format("%s,(STARTRACE/LCZ),startup&%s",version,dateformat.format(startTime)));
			}

			configure=new Configure();
//			String dltcfpath=context.getRealPath("/WEB-INF/myweb.properties");
//			File file0=new File(dltcfpath);
//			if(file0.exists()){
//				configure.loadFile(dltcfpath);
//			}
//			else if(configString!=null&&!(configString=configString.trim()).isEmpty()){
				if(configString.startsWith("ref:")){
					String path=configString.substring(4);
					File file1=new File(path);
					if(!file1.isAbsolute()){
						path=context.getRealPath(path);
					}
					configure.loadFile(path);
				}
				else{
					configure.loadConfigStr(configString);
				}
			//}

			HttpApplication application=new HttpApplication(context);
			HttpServerUtility server=new HttpServerUtility(application,configure);

			EnumSet<DispatcherType> dispats=EnumSet.of(DispatcherType.INCLUDE,DispatcherType.REQUEST,DispatcherType.FORWARD);
			//增加转换过滤器
			CaseFilter filter=new CaseFilter();
			FilterRegistration.Dynamic efr1=context.addFilter(CaseFilter.class.getName(), filter);
			efr1.setAsyncSupported(true);
			efr1.addMappingForUrlPatterns(dispats, true, "*");

			//增加Request监听器
			RequestListener reqlistener=new RequestListener();
			context.addListener(reqlistener);
			/*//增加动态编译Servlet
			HttpServlet servlet=new ComplierServlet();
			String servletName="stormweb_dynComplier";
			ServletRegistration.Dynamic esr=context.addServlet(servletName, servlet);
			esr.addMapping(server.getUrlPattern());*/

			//初始化并添加Servlet
			HttpServlet servlet=new MyHttpServlet();
			String servletName=servlet.getClass().getName();
			ServletRegistration.Dynamic esr=context.addServlet(servletName, servlet);
			esr.setAsyncSupported(true);
			//esr.addMapping(server.getUrlPattern());
			esr.addMapping(configure.getMVCSet().getUrlPattens());

			//添加Dynamic到attribute
			context.setAttribute(HttpRuntime.APP_SERVLETDYNAMIC, esr);

			if(server.getConfigure().getInterceptSet().enabled()){
				//初始化并添加拦截器
				final InterceptorFilter intcptFilter=new InterceptorFilter(context);
				FilterRegistration.Dynamic infr=context.addFilter(intcptFilter.getClass().getName(), intcptFilter);
				infr.setAsyncSupported(true);
				infr.addMappingForServletNames(dispats, true, servletName);

			}
			//初始化并添加页面缓存过滤器
			OutputCachedFilter cacheFilter=new OutputCachedFilter();
			FilterRegistration.Dynamic efr=context.addFilter(cacheFilter.getClass().getName(), cacheFilter);
			efr.setAsyncSupported(true);
			efr.addMappingForServletNames(dispats, true, servletName);

			application.start(null);
		}
		catch(Exception ex){
			context.setAttribute(HttpRuntime.APP_ERROR,ex);
			/*HttpServlet errSevlt=new HttpServlet(){
				private static final long serialVersionUID = 1L;
				public void service(HttpServletRequest req,HttpServletResponse resp)
				throws ServletException,IOException
				{
					Object ex=this.getServletContext().getAttribute(HttpRuntime.APP_ERROR);
					if(ex instanceof RuntimeException
							||ex instanceof ServletException
							||ex instanceof IOException){
						throw (RuntimeException)ex;
					}
					if(ex instanceof IOException){
						throw (IOException)ex;
					}
					if(ex instanceof ServletException){
						throw (ServletException)ex;
					}
				}
			};
			ServletRegistration.Dynamic esr=context.addServlet("stormweb-error", errSevlt);
			if(configure==null)
				esr.addMapping("*");
			else
				esr.addMapping(configure.getMVCSet().getUrlPattens());*/
		}
	}
	public static void Stop(ServletContext context){
		Object obj2=context.getAttribute(HttpRuntime.APP_ATTR_SERVER);
		if(obj2!=null){
			HttpServerUtility server=(HttpServerUtility)obj2;
			server.destroy();
		}
	}
}
