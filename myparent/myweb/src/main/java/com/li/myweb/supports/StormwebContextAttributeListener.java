package com.li.myweb.supports;

import java.io.IOException;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.HttpRuntime;

public class StormwebContextAttributeListener implements
		ServletContextAttributeListener {
	
	@Override
	public void attributeAdded(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub
		String name=arg0.getName();
		if(name.equals(HttpRuntime.APP_ERROR)){
			HttpServlet errSevlt=new HttpServlet(){
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
			ServletRegistration.Dynamic dynamic=(ServletRegistration.Dynamic)arg0.getServletContext().getAttribute(HttpRuntime.APP_SERVLETDYNAMIC);
			ServletRegistration.Dynamic esr=arg0.getServletContext().addServlet("stormweb-error", errSevlt);
			String[] errmapping;
			if(dynamic!=null){
				errmapping=new String[dynamic.getMappings().size()];
				errmapping=dynamic.getMappings().toArray(errmapping);
			}
			else{
				errmapping=new String[]{"*"};
			}
			try{
				esr.addMapping(errmapping);
			}catch(Exception e){
				
			}
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent arg0) {
		// TODO Auto-generated method stub

	}

}
