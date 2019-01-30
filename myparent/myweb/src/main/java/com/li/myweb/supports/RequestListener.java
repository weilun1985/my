package com.li.myweb.supports;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import com.li.myweb.HttpContext;
import com.li.myweb.HttpRuntime;

public class RequestListener implements ServletRequestListener {

	@Override
	public void requestDestroyed(ServletRequestEvent arg0) {
		// TODO Auto-generated method stub
		HttpContext.destroyCurrent();
	}

	@Override
	public void requestInitialized(ServletRequestEvent arg0) {
		// TODO Auto-generated method stub
		ClassLoader loader=(ClassLoader)arg0.getServletContext().getAttribute("APP_CLASSLOADER");
		if(loader!=null)
			Thread.currentThread().setContextClassLoader(loader);
		arg0.getServletRequest().setAttribute(HttpRuntime.REQATTR_ENTERTIME,System.currentTimeMillis());
	}

}
