package com.li.myweb.caching;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import com.li.myweb.HttpRequest;
import com.li.myweb.ResponseContent;
import com.li.myweb.Utils;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import com.li.myweb.HttpServerUtility;

public class OutputOSCache {
	class CacheContext{
		public CacheContext(String regId,int periods,int depends,boolean cce){
			this.periods=periods;
			this.depends=depends;
			this.regId=regId;
			this.cacheControlEnable=cce;
		}
		public final int periods;                 //缓存周期
		public final int depends;                 //缓存依赖
		public final String regId;                //应用程序路径
		public final boolean cacheControlEnable;
	}
	class CacheEntry implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6922193136986882742L;
		public long version;
		public ResponseContent content;
	}
	public class CacheKey{
		public CacheKey(String regId,String id){
			this.regId=regId;
			this.ID=id;
		}
		public final String regId;
		public final String ID;
		@Override
		public String toString(){
			return String.format("%s-%s", regId,ID);
		}
	}
	public static final Pattern REX_GZIP=Pattern.compile("gzip", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
	public OutputOSCache(HttpServerUtility server,Properties propertis){
		this.server=server;
		cacheAdmin=new GeneralCacheAdministrator(propertis);
		cacheContexts=new ConcurrentHashMap<String,CacheContext>();
	}
	private GeneralCacheAdministrator cacheAdmin;
	private ConcurrentHashMap<String,CacheContext> cacheContexts;
	private HttpServerUtility server;
	//private final static Pattern REX_ACTION=Pattern.compile("(\\?|&|^)_action=(\\w+)",Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);
	public CacheContext getContext(HttpServletRequest req){
		/*String regId=server.getEffectivePath(req.getRequestURI());
		String queryStr=req.getQueryString();
		if(queryStr!=null&&!queryStr.isEmpty()){
			Matcher m=REX_ACTION.matcher(req.getQueryString());
			if(m.find()){
				regId=regId+"@"+m.group(2);
			}
		}
		return regId;*/
		/*String actName=null;
		String registID=null;
		String effecPath=server.getEffectivePath(req.getRequestURI()).toLowerCase();
		String queryStr=req.getQueryString();
		if(queryStr!=null&&!queryStr.isEmpty()){
			Matcher m=REX_ACTION.matcher(req.getQueryString());
			if(m.find()){
				actName=m.group(2).toLowerCase();
			}
		}
		boolean noActionParam=(actName==null)&&(effecPath.lastIndexOf("/"))>0;
		CacheContext context;
		if(noActionParam){
			registID=effecPath;
		    context=this.cacheContexts.get(registID);
			if(context!=null)
				return context;
		}
	
		//如果不是无_action参数模式
		if(actName!=null&&!actName.isEmpty()){
			registID=effecPath+"/"+actName;
			return this.cacheContexts.get(registID);
		}
		//如果没有定义动作名
		
		//如果是HEAD方式，假如没有找到dohead的动作缓存定义，就采用doget的动作缓存定义
		
		if(req.getMethod().equals(HttpMethod.HEAD)){
			registID=effecPath+"/dohead";
			context=this.cacheContexts.get(registID);
			if(context!=null)
				return context;
			else{
				registID=effecPath+"/doget";
				context=this.cacheContexts.get(registID);
				if(context!=null)
					return context;
			}
		}
		else{
			registID=effecPath+"/doget";
			context=this.cacheContexts.get(registID);
			if(context!=null)
				return context;
		}
		return null;
		*/
		String registID=this.getRegId(req);
		return this.cacheContexts.get(registID);
	}
	public CacheContext getContext(CacheKey key){
		return this.cacheContexts.get(key.regId);
	}
	public CacheKey getkey(HttpServletRequest req){
		//String regId=getRegistID(req);
		CacheContext context=this.getContext(req);//cacheContexts.get(regId);
		if(context==null)
			return null;
		StringBuffer keyBuffer=new StringBuffer();
		keyBuffer.append(context.regId);
		//如果缓存依赖于参数，源字符串中增加参数值，排除掉指定动作的参数_action
		if((context.depends&PageCacheDepends.QUERYSTRING)==PageCacheDepends.QUERYSTRING){
			String qstro=req.getQueryString();
			/*if(qstro!=null&&!qstro.isEmpty()){
				Matcher m=REX_ACTION.matcher(qstro);
				if(m.find()){
					int s=m.start();
					int e=m.end();
					if(s==0&&e==qstro.length()){
						qstro=null;
					}
					if(s==0)
						qstro=qstro.substring(e+1).toLowerCase();
					else if(e==qstro.length()){
						qstro=qstro.substring(0,s);
					}
					else{
						qstro=qstro.substring(0,s)+qstro.substring(e+1);
					}
				}else{
					qstro=qstro.toLowerCase();
				}
			}*/
			keyBuffer.append("#"+qstro);
		}
		//如果缓存依赖于Cookie，源字符串中增加Cookie值
		if((context.depends&PageCacheDepends.COOKIE)==PageCacheDepends.COOKIE){
			Cookie[] cookies=req.getCookies();
			for(Cookie cook:cookies){
				keyBuffer.append("#"+cook.getValue());
			}
		}
		//如果缓存与Session有关系
		if((context.depends&PageCacheDepends.SESSION)==PageCacheDepends.SESSION){
			keyBuffer.append("#"+req.getSession().getId());
		}
		//根据源字符串按MD5算法计算CacheKey
		try {
			String saveId=Utils.md5String(keyBuffer.toString());
			CacheKey cacheKey=new CacheKey(context.regId,saveId);
			return cacheKey;
		} 
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("deprecation")
	public long hit(CacheKey key,int periods,HttpServletRequest req,HttpServletResponse resp) throws IOException{
		//CacheContext context=cacheContexts.get(key.regId);
		try {
			CacheEntry entry=(CacheEntry)this.cacheAdmin.getFromCache(key.ID, periods);
			if(entry==null)
				return 0;
			String lmstr=req.getHeader("If-Modified-Since");
			if(lmstr!=null&&lmstr.length()>0){
				long lmt=0;
				//将字符串转换为long
				if(Pattern.matches("^\\d+$", lmstr)){
					lmt=Long.parseLong(lmstr);
				}
				else{
					lmt=Date.parse(lmstr);
				}
				if(lmt==entry.version){
					resp.setStatus(304);
					return entry.version;
				}
			}
			entry.content.WriteTo(false,resp);
			resp.setHeader("Last-Modified", entry.version+"");
			/*if(context.cacheControlEnable){
				long age=((context.periods*1000+entry.version)-System.currentTimeMillis())/1000;
				resp.setHeader("Cache-Control", "max-age="+age);
			}*/
			return entry.version;
		} catch (NeedsRefreshException e) {
			this.cacheAdmin.cancelUpdate(key.ID);
			return 0;
		}
	}
	public void set(long version,CacheKey key,CacheHttpServletResponseWrapper wresp){
		ResponseContent respContent=wresp.getContent();
    	CacheEntry entry=new CacheEntry();
    	entry.version=version;
    	entry.content=respContent;
    	this.cacheAdmin.putInCache(key.ID, entry);
    }
	public long set(CacheKey key,CacheHttpServletResponseWrapper wresp){
		long version=System.currentTimeMillis();
		this.set(version, key, wresp);
		return version;
	}
	public String register(HttpServletRequest req,int periods,int depends,boolean cacheControl){
		String regId=this.getRegId(req);
		CacheContext context=new CacheContext(regId,periods,depends,cacheControl);
		this.cacheContexts.put(regId, context);
		return regId;
	}
	public String getRegId(HttpServletRequest req){
		String action=req.getParameter(HttpRequest.ACTIONPARAME_NAME);
		int ctxlen=req.getServletContext().getContextPath().length();
		String regid=req.getRequestURI().substring(ctxlen);
		int a=regid.lastIndexOf('.');
		if(a>-1){
			regid=regid.substring(0,a);
		}
		if(action!=null){
			regid=regid+"/"+action;
		}
		return regid;
	}
	public void destroy(){
		this.cacheAdmin.destroy();
		this.cacheContexts=null;
	}
}
