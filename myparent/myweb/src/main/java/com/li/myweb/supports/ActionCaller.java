package com.li.myweb.supports;

import java.io.Writer;
import java.util.Map;

import com.li.myweb.HttpContext;
import com.li.myweb.HttpMethod;
import com.li.myweb.HttpRequest;
import com.li.myweb.HttpResponse;
import com.li.myweb.HttpServerUtility;

public abstract class ActionCaller {
	public final static String SYSVALI_CONTROLLER="__THIS";
	public final static String SYSVALI_APPCONTEXT="__ROOT";
	public final static String SYSVALI_SERVER="__SERVER";
	public ActionCaller(){
		this.httpContext=HttpContext.current();
		req=httpContext.request();
		resp=httpContext.response();
		server=httpContext.server();
		_http_method=req.getMethod();
		_outmap=new java.util.HashMap<String,Object>();
		if(_http_method.equalsIgnoreCase(HttpMethod.GET))
			_pm_map=req.queryStrings();
		else
			_pm_map=req.forms();
	}
	protected final HttpRequest req;
	protected final HttpResponse resp;
	protected final HttpServerUtility server;
	protected final HttpContext httpContext;
	protected final String _http_method;
	protected Map<String,Object> _outmap;
	protected Writer _writer;
	protected Map<String,String[]> _pm_map;
	protected Object _result;
	public abstract void call() throws Throwable;
	public abstract void render() throws Throwable;
	public void add(String key,Object obj){
		this._outmap.put(key, obj);
	}
	public void addAll(Map<String,Object> m){
		this._outmap.putAll(m);
	}
	public Map<String,Object> getDataContext(){
		return this._outmap;
	}
}
