package com.li.myweb.supports;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.HttpContext;
import com.li.myweb.HttpRequest;
import com.li.myweb.HttpRuntime;
import com.li.myweb.HttpServerUtility;
import com.li.myweb.exceptions.ActionFlushImme;
import com.li.myweb.exceptions.ActionInterrupt;

public class StormwebHttpServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionCallerManager callerManager;
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		HttpServerUtility server=(HttpServerUtility)config.getServletContext().getAttribute(HttpRuntime.APP_ATTR_SERVER);
		try {
			callerManager=new ActionCallerManager(server);
		} catch (UnsupportedEncodingException e) {
			throw new ServletException(e);
		}
	}
	public void service(HttpServletRequest req,HttpServletResponse resp)
	throws ServletException,IOException
	{
		HttpContext httpContext=new HttpContext(req,resp);
		HttpRequest req2=httpContext.request();
		try {
			ActionCaller caller;
			try{
				caller=callerManager.getCaller(req2);
			}
			catch(Exception e){
				do{
					Exception ei=(Exception)e.getCause();
					if(ei==null)
						break;
					e=ei;
				}while(true);
				if(e instanceof ClassNotFoundException){
					resp.sendError(404);
					return;
				}
				else if(e instanceof NoSuchMethodException){
					resp.sendError(501);
					return;
				}
				else if(e instanceof IllegalAccessException){
					//403,拒绝访问
					resp.sendError(403,req.getRequestURI());
					return;
				}
				else if(e instanceof SecurityException){
					resp.sendError(403,req.getRequestURI());
					return;
				}
				throw e;
			}
			/*catch(ClassNotFoundException e){
				resp.sendError(404);
				return;
			}
			catch(NoSuchMethodException e){
				resp.sendError(501);
				return;
			}
			catch(IllegalAccessException e){
				//403,拒绝访问
				resp.sendError(403,req.getRequestURI());
				return;
			}
			catch(SecurityException e){
				//403,拒绝访问
				resp.sendError(403,req.getRequestURI());
				return;
			}*/
			//执行动作
			long ts_invoke=System.currentTimeMillis();
			try{
				caller.call();
			}catch(ActionFlushImme e){
				
			}catch(ActionInterrupt e){
				return;
			}
			finally{
				httpContext.setTimeInvoke(System.currentTimeMillis()-ts_invoke);
			}
			//输出
			ts_invoke=System.currentTimeMillis();
			try{
				if(resp.getStatus()==200)
					caller.render();
			}finally{
				httpContext.setTimeRender(System.currentTimeMillis()-ts_invoke);
			}
		} catch (Throwable e) {
			do{
				Exception ei=(Exception)e.getCause();
				if(ei==null)
					break;
				e=ei;
			}while(true);
			
			if(e instanceof RuntimeException)
				throw (RuntimeException)e;
			else if(e instanceof IOException)
				throw (IOException)e;
			else if(e instanceof ServletException)
				throw (ServletException)e;
			throw new ServletException(e.getMessage());
		}
		finally{
			//HttpContext.current().destroy();
			httpContext.flushTimeHeader();
		}
	}
}
