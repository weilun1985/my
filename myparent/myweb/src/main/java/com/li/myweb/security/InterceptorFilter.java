package com.li.myweb.security;

import java.io.IOException;
//import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.li.myweb.Configure.Intercept;
import com.li.myweb.HttpRuntime;
import com.li.myweb.HttpServerUtility;
import com.li.myweb.Utils;

//请求安全拦截器，屏蔽频繁请求的客户端
public class InterceptorFilter implements Filter {
	public InterceptorFilter(ServletContext context){
		this.server=(HttpServerUtility)context.getAttribute(HttpRuntime.APP_ATTR_SERVER);
	    this.interceptorInfo=this.server.getConfigure().getInterceptSet();//this.server.getInterceptorInfo();
	    if(this.interceptorInfo.isAutoIntercept()){
	    	this.autoblacklist=new ConcurrentHashMap<Long,BlacklistEntry>();
	    	this.accessmap=new ConcurrentHashMap<Long,AccessedEntry>();
	    }
	    if(this.interceptorInfo.getBlackList()!=null){
	    	String path=this.interceptorInfo.getBlackList();
	    	if(path.startsWith("http")){
	    		this.blackList=new RemoteList(path);
	    	}
	    	else{
	    		path=this.server.mapPath(path);
		    	this.blackList
		    		=new LocalFileList(path);
	    	}
	    }
	    if(this.interceptorInfo.getWhiteList()!=null){
	    	String path=this.interceptorInfo.getWhiteList();
	    	if(path.startsWith("http")){
	    		this.whiteList=new RemoteList(path);
	    	}
	    	else{
	    		path=this.server.mapPath(path);
		    	this.whiteList
		    		=new LocalFileList(path);
	    	}
	    }
	}
	class AccessedEntry{
		public AccessedEntry(long ip){
			this.ip=ip;
			this.createTime=System.currentTimeMillis();
			this.lastAccessTime=this.createTime;
			this.count=new AtomicInteger();
			this.count.incrementAndGet();
		}
		public final long ip;
		public final long createTime;
		private long lastAccessTime;
		private AtomicInteger count;
		private int[] history=new int[60];    //60s内的历史记录
		public long getLastAccessTime(){
			return this.lastAccessTime;
		}
		public void setLastAccessTime(){
			this.lastAccessTime=System.currentTimeMillis();
			int count=this.count.incrementAndGet();
			history[(int)((this.lastAccessTime/1000)%history.length)]=count;
		}
		public int getCountPerMinu(){
			/*
			long diff=(this.lastAccessTime-this.createTime)/60000;
			if(diff==0)
				return this.count.get();
			return (int)(this.count.get()/diff);
			*/
			int[] hisshadow=Arrays.copyOf(this.history, 60);
			Arrays.sort(hisshadow);
			//如果最大的值为0，则直接返回0
			if(hisshadow[59]==0){
				return 0;
			}
			//寻找最小的非0值的位置
			int minIndex=0;
			for(int i=0;i<hisshadow.length;i++){
				if(hisshadow[i]>0){
					minIndex=i;
					break;
				}
			}
			return hisshadow[59]-hisshadow[minIndex];
			
		}
	}
	class BlacklistEntry{
		public BlacklistEntry(long ip){
			this.ip=ip;
			this.createTime=System.currentTimeMillis();
		}
		public final long ip;
		public final long createTime;
		public boolean overdue(int expireMinu){
			return (System.currentTimeMillis()-this.createTime)/60000>expireMinu;
		}
	}
	private ConcurrentHashMap<Long,BlacklistEntry> autoblacklist;        //黑名单
	private ConcurrentHashMap<Long,AccessedEntry> accessmap;         //访问统计器
	private Intercept interceptorInfo;
	private HttpServerUtility server;
	private InterecptorList whiteList;
	private InterecptorList blackList;
	private Thread thr_1;
	private Thread thr_2;
	public boolean isListCheckUpdate(){
		return this.whiteList!=null||this.blackList!=null;
	}
	public void checkListUpdate(){
		if(this.whiteList!=null)
			this.whiteList.checkUpdate();
		if(this.blackList!=null)
			this.blackList.checkUpdate();
	}
	public boolean isAutoInterept(){
		return this.interceptorInfo.isAutoIntercept();
	}
	//自动判定
	public void autoJudge(){
		ArrayList<Long> removeA=new ArrayList<Long>();    //需要进行删除的访问记录
		for(AccessedEntry entry:accessmap.values()){
			//如果最后访问时间距离现在超出30分钟，删除访问记录
			if(System.currentTimeMillis()-entry.lastAccessTime>1000*60*30){
				removeA.add(entry.ip);
				continue;
			}
			//如果一分钟内的访问数超过阀值，则加入黑名单，同时删除访问记录
			if(entry.getCountPerMinu()>this.interceptorInfo.getAutoAccessLimit()){
				removeA.add(entry.ip);
				BlacklistEntry blackItem=new BlacklistEntry(entry.ip);
				this.autoblacklist.putIfAbsent(entry.ip, blackItem);
			}
		}
		for(long ip:removeA){
			accessmap.remove(ip);
		}
	}
	private boolean destroied=false;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		this.autoblacklist=null;
		this.accessmap=null;
		this.blackList=null;
		this.whiteList=null;
		this.destroied=true;
		if(this.thr_1!=null)
			this.thr_1.interrupt();
		if(this.thr_2!=null)
			this.thr_2.interrupt();
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		long t=System.currentTimeMillis();
		arg0.setAttribute(HttpRuntime.REQATTR_ENTERTIME_INTERCEPTOR, t);
		if(arg0.getAttribute(HttpRuntime.REQATTR_ENTERTIME)==null)
			arg0.setAttribute(HttpRuntime.REQATTR_ENTERTIME, t);
		HttpServletRequest req=(HttpServletRequest)arg0;
		HttpServletResponse resp=(HttpServletResponse)arg1;
		//获取请求IP
		String ipStr=req.getRemoteAddr();
		InetAddress ip=InetAddress.getByName(ipStr);
		//Inet4Address ip=(Inet4Address)Inet4Address.getByName(ipStr);
		long ipLong=Utils.readLong(ip.getAddress());
		//检查白名单
		if(this.whiteList!=null&&this.whiteList.contains(ipLong)){
			chain.doFilter(arg0, arg1);
			return;
		}
		//String version=(String)this.server.getApplication().getAttribute(HttpRuntime.APP_DISPLAYINFO);
		//检查黑名单
		if(this.blackList!=null&&this.blackList.contains(ipLong)){
			resp.setHeader("eagle-obstruct","in blacklist");
			//resp.setHeader("E-Framework", version);
			resp.sendError(403, "您的访问已经被屏蔽！");
			return;
		}
		//如果启用了自动屏蔽功能
		if(this.interceptorInfo.isAutoIntercept()){
			//首先检查黑名单中是否有该IP
			BlacklistEntry blackItem=autoblacklist.get(ipLong);
			if(blackItem!=null){
				//如果已经过了黑名单有效期，则删除黑名单
				//否则返回禁止访问
				if(blackItem.overdue(this.interceptorInfo.getAutoForbidExp())){
					autoblacklist.remove(ipLong);
				}
				else{
					resp.setHeader("eagle-obstruct", blackItem.createTime+"");
					//resp.setHeader("E-Framework",version);
					resp.sendError(403, "请求过于频繁，系统已自动屏蔽你的请求，请稍候再试！");
					return;
				}
			}
			//获取访问统计项
			AccessedEntry accEntry=this.accessmap.get(ipLong);
			if(accEntry==null){
				accEntry=new AccessedEntry(ipLong);
				this.accessmap.putIfAbsent(ipLong, accEntry);
			}
			else{
				accEntry.setLastAccessTime();
				/*
				//如果超过阀值，加入黑名单，同时删除访问记录
				if(accEntry.getCountPerMinu()>this.interceptorInfo.autoAccessLimit){
					blackItem=new BlacklistEntry(ipLong);
					this.autoblacklist.putIfAbsent(ipLong, blackItem);
					resp.setHeader("eagle-obstruct","auto forbid at "+ blackItem.createTime);
					resp.setHeader("Framework", HttpRuntime.VERSION);
					resp.sendError(403, "请求过于频繁，系统已自动屏蔽你的请求，请稍候再试！");
					//移除访问记录
					this.accessmap.remove(ipLong);
					return;
				}
				*/
			}
		}
		//继续向后访问
		chain.doFilter(arg0, arg1);
	}
	@Override
	public void init(FilterConfig config) throws ServletException {
		/*
		this.server=(HttpServerUtility)config.getServletContext().getAttribute(HttpRuntime.APP_ATTR_SERVER);
	    this.interceptorInfo=this.server.getInterceptorInfo();
	    if(this.interceptorInfo.autoIntercept){
	    	this.autoblacklist=new ConcurrentHashMap<Long,BlacklistEntry>();
	    	this.accessmap=new ConcurrentHashMap<Long,AccessedEntry>();
	    }
	    if(this.interceptorInfo.blackListFile!=null){
	    	String path=this.interceptorInfo.blackListFile;
	    	if(path.startsWith("http")){
	    		this.blackList=new RemoteList(path);
	    	}
	    	else{
	    		path=this.server.mapPath(path);
		    	this.blackList
		    		=new LocalFileList(path);
	    	}
	    }
	    if(this.interceptorInfo.whiteListFile!=null){
	    	String path=this.interceptorInfo.whiteListFile;
	    	if(path.startsWith("http")){
	    		this.whiteList=new RemoteList(path);
	    	}
	    	else{
	    		path=this.server.mapPath(path);
		    	this.whiteList
		    		=new LocalFileList(path);
	    	}
	    }
	    */
		final InterceptorFilter intcptFilter=this;
		//如果拦截器可以对黑/白名单进行更新检查
		if(this.isListCheckUpdate()){
			Runnable runnable=new Runnable(){
				public void run(){
						while(!intcptFilter.destroied){
							try {
								Thread.sleep(3000);
								intcptFilter.checkListUpdate();
							} catch (InterruptedException e) {
								break;
							}
							catch(Exception e){
								
							}
						}
					}
				};
			//启动更新检查线程
			thr_1 =  new Thread(runnable);
			thr_1.setDaemon(true);
			thr_1.start();
		}
		//如果启用了自动屏蔽功能，则启动访问记录定时清理
		if(intcptFilter.isAutoInterept()){
			Runnable runnable=new Runnable(){
				public void run(){
						while(!intcptFilter.destroied){
							try {
								Thread.sleep(10000);
								intcptFilter.autoJudge();
							} catch (InterruptedException e) {
								break;
							}
							catch(Exception e){
								
							}
						}
					}
				};
			//启动自动清理线程
			thr_2 =  new Thread(runnable);
			thr_2.setDaemon(true);
			thr_2.start();
		}
	}
}
