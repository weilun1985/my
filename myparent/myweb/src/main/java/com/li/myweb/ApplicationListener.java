package com.li.myweb;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.li.myweb.supports.AppController;

@WebListener
public class ApplicationListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context= arg0.getServletContext();
		/*if(context.getAttribute(HttpRuntime.APP_STARTTIME)!=null)
			return;
		//设置启动时间
		Date startTime=new Date();
		context.setAttribute(HttpRuntime.APP_STARTTIME,startTime);
		if(context.getAttribute(HttpRuntime.APP_DISPLAYINFO)==null){
			SimpleDateFormat dateformat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			String file=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			String version=file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
			context.setAttribute(HttpRuntime.APP_DISPLAYINFO, String.format("%s,(STARTRACE/LCZ),startup&%s",version,dateformat.format(startTime)));
		}*/
        AppController.Startup(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        AppController.Stop(arg0.getServletContext());
    }



}
