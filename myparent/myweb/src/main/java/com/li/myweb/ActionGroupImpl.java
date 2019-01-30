package com.li.myweb;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.li.myweb.templates.Template;
/*import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;*/

import com.li.myweb.exceptions.ActionFlushImme;
import com.li.myweb.exceptions.ActionInterrupt;

@Deprecated
public abstract class ActionGroupImpl {
	public ActionGroupImpl(){
		HttpContext context=HttpContext.current();
		if(context!=null){
			this.httpContext=context;
			this.request=context.request();
			this.response=context.response();
			this.server=context.server();
		}
		//templateContext=new VelocityContext();
	}
	protected HttpRequest request;                  //Response对象
	protected HttpResponse response;                //Request对象
	protected HttpServerUtility server;			  //Servlet对象
	protected HttpContext httpContext;		      //当前的HttpContext
	//protected Context templateContext;
	protected Map<String,Object> templateContext;
	public void init(){
		if(this.httpContext==null){
			HttpContext context=HttpContext.current();
			this.httpContext=context;
			this.request=context.request();
			this.response=context.response();
			this.server=context.server();
		}
		templateContext=new HashMap<String,Object>();
	}
	//添加数据到模板引擎容器
	protected void add(String name,Object value){
		this.templateContext.put(name, value);
	}
	public void mergeOut(Template temp,Writer writer){
		//temp.merge(this.templateContext, writer);
		temp.render(this.templateContext, writer);
	}
	//停止当前页面执行
	protected void stop(){
		throw new ActionInterrupt();
	}
	//完成当前页面执行并马上输出
	protected void compleate(){
		throw new ActionFlushImme();
	}
}
