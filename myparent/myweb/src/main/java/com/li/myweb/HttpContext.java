package com.li.myweb;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.caching.OSCache;
import com.li.myweb.exceptions.ActionFlushImme;
import com.li.myweb.exceptions.ActionInterrupt;

public class HttpContext {
	private final static ThreadLocal<HttpContext> _THREADLOCAL=new ThreadLocal<HttpContext>();
	
	public HttpContext(HttpServletRequest req,HttpServletResponse resp) throws UnsupportedEncodingException{
		application=(HttpApplication)req.getServletContext().getAttribute(HttpRuntime.APP_ATTR_APP);
		server=(HttpServerUtility)application.getAttribute(HttpRuntime.APP_ATTR_SERVER);
		req.setAttribute(HttpRuntime.REQATTR_ENTERTIME_HTTPSERVLET, System.currentTimeMillis());
		request=new HttpRequest(req,this.server.getRequestEncoding());
		response=new HttpResponse(resp,this.server.getResponseEncoding());
		
		
		this.createTime=System.currentTimeMillis();
		if(req.getAttribute(HttpRuntime.REQATTR_ENTERTIME)==null){
			this.enterTime=this.createTime;
			req.setAttribute(HttpRuntime.REQATTR_ENTERTIME, this.enterTime);
		}
		else{
			this.enterTime=(Long)req.getAttribute(HttpRuntime.REQATTR_ENTERTIME);
		}
		_THREADLOCAL.set(this);
	}
	private HttpRequest request;
	private HttpResponse response;
	private HttpApplication application;
	private HttpServerUtility server;
	
	private Long enterTime;
	private Long createTime;
	//private Long destoryTime;
	private Long timeInvoke;
	private Long timeRender;

	public void setTimeInvoke(long time){
		if(this.timeInvoke==null)
			this.timeInvoke=time;
		else
			this.timeInvoke+=time;
	}
	public void setTimeRender(long time){
		if(this.timeRender==null)
			this.timeRender=time;
		else
			this.timeRender+=time;
	}
	public void flushTimeHeader(){
		if(this.response.getStatus()==200||this.response.getStatus()==304){
			StringBuffer sb=new StringBuffer();
			//sb.append(String.format("%d-%d-%d", this.enterTime,this.createTime-this.enterTime,this.destoryTime-this.createTime));
			sb.append(String.format("%d-%d", this.enterTime,this.createTime));
			if(this.timeInvoke!=null)
				sb.append(" invoke="+this.timeInvoke);
			if(this.timeRender!=null)
				sb.append(" render="+this.timeRender);
			this.response.setHeader("Timeline", sb.toString());
		}
	}
	
	public static HttpContext current(){
		return _THREADLOCAL.get();
	}
	public static void destroyCurrent(){
		//_THREADLOCAL.set(null);
		_THREADLOCAL.remove();
	}
	
	public HttpApplication application(){
		return this.application;
	}
	public HttpServerUtility server(){
		return this.server;
	}
	public HttpRequest request(){
		return request;
	}
	public HttpResponse response(){
		return response;
	}
	/*public void destroy(){
		this.destoryTime=System.currentTimeMillis();
		this.setTimeHeader();
		_THREADLOCAL.set(null);
	}*/
	public OSCache getCache(){
		return this.server.getCache();
	}
	
	//停止当前执行
	public void stop(){
		throw new ActionInterrupt();
	}
	//完成当前执行并马上输出
	public void compleate(){
		throw new ActionFlushImme();
	}
}
