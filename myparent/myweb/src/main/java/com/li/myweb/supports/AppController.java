package com.li.myweb.supports;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.Configure;
import com.li.myweb.HttpApplication;
import com.li.myweb.HttpRuntime;
import com.li.myweb.HttpServerUtility;
import com.li.myweb.caching.OutputCachedFilter;
import com.li.myweb.security.InterceptorFilter;

public class AppController {
	public static void Startup(ServletContext context){
		String configString=context.getInitParameter(HttpRuntime.FRAME_CONFIG_PAREMNAME);
		Configure configure=null;
		StormwebContextAttributeListener scal=new StormwebContextAttributeListener();
		context.addListener(scal);
		try
		{
			if(context.getattribute(httpruntime.app_starttime)!=null)
				return;
			//设置启动时间
			date starttime=new date();
			context.setattribute(httpruntime.app_starttime,starttime);
			if(context.getattribute(httpruntime.app_displayinfo)==null){
				simpledateformat dateformat = new simpledateformat("yy-mm-dd hh:mm:ss");
				string file=appcontroller.class.getprotectiondomain().getcodesource().getlocation().getpath();
				string version=file.substring(file.lastindexof("/")+1,file.lastindexof("."));
				context.setattribute(httpruntime.app_displayinfo, string.format("%s,(startrace/lcz),startup&%s",version,dateformat.format(starttime)));
			}
			
			configure=new configure();
			string dltcfpath=context.getrealpath("/web-inf/stormweb.properties");
			file file0=new file(dltcfpath);
			if(file0.exists()){
				configure.loadfile(dltcfpath);
			}
			else if(configstring!=null&&!(configstring=configstring.trim()).isempty()){
				if(configstring.startswith("ref:")){
					string path=configstring.substring(4);
					file file1=new file(path);
					if(!file1.isabsolute()){
						path=context.getrealpath(path);
					}
					configure.loadfile(path);
				}
				else{
					configure.loadconfigstr(configstring);
				}
			}
			
			httpapplication application=new httpapplication(context);
			httpserverutility server=new httpserverutility(application,configure);
			
			enumset<dispatchertype> dispats=enumset.of(dispatchertype.include,dispatchertype.request,dispatchertype.forward);
			//增加转换过滤器
			casefilter filter=new casefilter();
			filterregistration.dynamic efr1=context.addfilter(casefilter.class.getname(), filter);
			efr1.setasyncsupported(true);
			efr1.addmappingforurlpatterns(dispats, true, "*");
			
			//增加request监听器
			requestlistener reqlistener=new requestlistener();
			context.addlistener(reqlistener);
			/*//增加动态编译servlet
			httpservlet servlet=new complierservlet();
			string servletname="stormweb_dyncomplier";
			servletregistration.dynamic esr=context.addservlet(servletname, servlet);
			esr.addmapping(server.geturlpattern());*/
			
			//初始化并添加servlet
			httpservlet servlet=new stormwebhttpservlet();
			string servletname=servlet.getclass().getname();
			servletregistration.dynamic esr=context.addservlet(servletname, servlet);
			esr.setasyncsupported(true);
			//esr.addmapping(server.geturlpattern());
			esr.addmapping(configure.getmvcset().geturlpattens());
			
			//添加dynamic到attribute
			context.setattribute(httpruntime.app_servletdynamic, esr);
			
			if(server.getconfigure().getinterceptset().enabled()){
				//初始化并添加拦截器
				final interceptorfilter intcptfilter=new interceptorfilter(context);
				filterregistration.dynamic infr=context.addfilter(intcptfilter.getclass().getname(), intcptfilter);
				infr.setasyncsupported(true);
				infr.addmappingforservletnames(dispats, true, servletname);
				
			}
			//初始化并添加页面缓存过滤器
			outputcachedfilter cachefilter=new outputcachedfilter();
			filterregistration.dynamic efr=context.addfilter(cachefilter.getclass().getname(), cachefilter);
			efr.setasyncsupported(true);
			efr.addmappingforservletnames(dispats, true, servletname);
			
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
