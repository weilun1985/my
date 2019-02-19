package com.li.myweb;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
//import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
//import java.net.URLDecoder;
//import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//import java.util.List;
//import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.li.myweb.annotation.Layout;
import com.li.myweb.annotation.Master;
import com.li.myweb.annotation.OutputCache;
import com.li.myweb.caching.OSCache;
import com.li.myweb.caching.OutputOSCache;
import com.li.myweb.exceptions.DefParamsUnmatchException;
import com.li.myweb.exceptions.TemplateException;
import com.li.myweb.supports.ActionGroupInfo;
import com.li.myweb.supports.ActionInfo;
import com.li.myweb.supports.ActionWrapper;
import com.li.myweb.templates.Template;

public class HttpServerUtility {
	//private final static String DEFAULT_CFGSTR="enc:req=utf-8,resp=utf-8,resph=utf-8;mvc:vmroot=\\views\\,urlpatten=*.do;oscache:memory=true,capacity=10000;outputcache:memory=true,capacity=10000;";
	public HttpServerUtility(HttpApplication application,Configure configure) throws IOException{
		this.application=application;
		this.applicationRoot=application.getRealPath("");
		this.configure=configure;
		this.requestEncoding=this.configure.getEncodeSet().getReqEnc();
		this.responseEncdoing=this.configure.getEncodeSet().getRespEnc();
		this.responseHeaderEncoding=this.configure.getEncodeSet().getResphEnc();
		//this.contrlJar=this.configure.getMVCSet().getControllerJAR();
		this.contrlPkg=this.configure.getMVCSet().getControllerPKG();
		application.setAttribute(HttpRuntime.APP_CONTRLPACKAGE, this.contrlPkg);
		this.templateRoot=this.mapPath("/WEB-INF/views/");

//		this.templateRoot=this.configure.getMVCSet().getVMRoot();
//		if(this.templateRoot==null||(this.templateRoot=this.templateRoot.trim()).length()==0){
//			this.templateRoot=this.mapPath("/WEB-INF/views/");
//			File file=new File(this.templateRoot);
//			if(!file.exists()){
//				String tmp2=this.mapPath("/views/");
//				file=new File(tmp2);
//				if(file.exists())
//					this.templateRoot=tmp2;
//			}
//		}
//		else{
//			this.templateRoot=this.application.getRealPath(this.templateRoot);
//		}
		this.igpath=this.configure.getMVCSet().getIgPath();
		//this.urlpattern=this.configure.getMVCSet().getUrlPatten().toLowerCase();
		/*String urlpatten=this.configure.getMVCSet().getUrlPatten();
		if(urlpatten!=null){
			String[] pattnsO=Utils.split(urlpatten,"|");
			ArrayList<String> pattnList=new ArrayList<String>();
			for(String p:pattnsO){
				if(p.isEmpty())
					continue;
				String[] all=Utils.allULCompounding(p);
				for(String itm:all){
					pattnList.add(itm);
				}
			}
			urlpatterns=new String[pattnList.size()];
			pattnList.toArray(urlpatterns);
		}*/
		//this.urlpatterns=this.configure.getMVCSet()..getUrlPattens();

		int iglen=this.application.getContextPath().length();
		if(this.igpath!=null)
			iglen+=this.igpath.length();
		if(iglen>1)
			this.uriSubIndex=iglen;
		
		//this.initVtlEngine();
		this.initTemplateEngine();
		this.initOSCahce();
		this.initOutputCache();
		application.setAttribute(HttpRuntime.APP_ATTR_SERVER, this);
		this.scan();
	}
	
	/*private void setConfig(String configStr){
		
		String[] groups=configStr.split(";");
		for(String itm:groups){
			String itmtm=itm.trim();
			if(itmtm.isEmpty())
				continue;
			
			String[] nv=itmtm.split(":",2);
			if(nv.length<2)
				continue;
			String groupName=nv[0].trim();
			if(groupName.equals("enc")&&!(nv[1]=nv[1].trim()).isEmpty()){
				getEncodingParams(nv[1]);
			}
			else if(groupName.equals("mvc")&&!(nv[1]=nv[1].trim()).isEmpty()){
				getMVCParams(nv[1]);
			}
			else if(groupName.equals("oscache")&&!(nv[1]=nv[1].trim()).isEmpty()){
				getOSCacheParams(nv[1]);
			}
			else if(groupName.equals("outputcache")&&!(nv[1]=nv[1].trim()).isEmpty()){
				getOutputCacheParams(nv[1]);
			}
			else if(groupName.equals("holdup")&&!(nv[1]=nv[1].trim()).isEmpty()){
				getHoldupParams(nv[1]);
			}
		}
	}*/
	/*private void getEncodingParams(String str){
		String[] items=str.split(",");
		for(String itm:items){
			String[] kv=itm.split("=",2);
			if(kv.length<2)
				continue;
			if(kv[0].equals("req"))
				requestEncoding=kv[1];
			else if(kv[0].equals("resp"))
				responseEncdoing=kv[1];
			else if(kv[0].equals("resph"))
				responseHeaderEncoding=kv[1];
		}
	}*/
	/*private void getMVCParams(String str){
		String[] items=str.split(",");
		String urlpattn=null;
		for(String itm:items){
			String[] kv=itm.split("=",2);
			if(kv.length<2)
				continue;
			if(kv[0].equals("jar"))
				contrlJar=kv[1];
			else if(kv[0].equals("pkg"))
				contrlPkg=kv[1];
			else if(kv[0].equals("vmroot")){
				vmroot=this.application.getRealPath(kv[1]);
			}
			else if(kv[0].equals("igpath"))
				igpath=kv[1];
			else if(kv[0].equals("urlpatten")){
				urlpattn=kv[1];
			}
		}
		
		if(urlpattn!=null){
			String[] pattnsO=Utils.split(urlpattn,"|");
			
			ArrayList<String> pattnList=new ArrayList<String>();
			for(String p:pattnsO){
				if(p.isEmpty())
					continue;
				String[] all=Utils.allULCompounding(p);
				for(String itm:all){
					pattnList.add(itm);
				}
			}
			urlpatterns=new String[pattnList.size()];
			pattnList.toArray(urlpatterns);
		}
	}*/
	/*private void getOSCacheParams(String str){
		String[] items=str.split(",");
		for(String itm:items){
			String[] kv=itm.split("=",2);
			if(kv.length<2)
				continue;
			oscacheProperties.put("cache."+kv[0], kv[1]);
		}
	}
	private void getOutputCacheParams(String str) {
		String[] items=str.split(",");
		for(String itm:items){
			String[] kv=itm.split("=",2);
			if(kv.length<2)
				continue;
			outputcacheProperties.put("cache."+kv[0], kv[1]);
		}
	}
	private void getHoldupParams(String str){
		this.intceptorInfo=new InterceptorInfo(str);
	}*/
	private void initTemplateEngine(){
		if(templateRoot!=null){
			//this.templateEngine=new TemplateEngine(this.applicationRoot,this.templateRoot,this.responseEncdoing);
			this.templateEngine=new TemplateEngine(this.configure.getProperties(),this.templateRoot,this.responseEncdoing);
			this.application.setAttribute(HttpRuntime.APP_TEPMLATEENGINE, this.templateEngine);
		}
	}
	/*private void initVtlEngine(){
		if(vmroot!=null){
			this.vtlEngine=new VelocityEngine();
			this.vtlEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, vmroot);  
			this.vtlEngine.setProperty(VelocityEngine.ENCODING_DEFAULT, "UTF-8");				
			this.vtlEngine.setProperty(VelocityEngine.INPUT_ENCODING, "UTF-8");				    
			this.vtlEngine.setProperty(VelocityEngine.OUTPUT_ENCODING, "UTF-8");				
			this.vtlEngine.init();
			this.application.setAttribute(HttpRuntime.APP_TEPMLATEENGINE, this.vtlEngine);
		}
	}*/
	private void initOSCahce(){
		//this.oscache=new OSCache(this.oscacheProperties);
		this.oscache=new OSCache(this.configure.getObjCacheSet().getProperties());
		this.application.setAttribute(HttpRuntime.APP_ATTR_OSCACHE, this.oscache);
	}
	private void initOutputCache(){
		//this.outputcache=new OutputOSCache(this,this.outputcacheProperties);
		this.outputcache=new OutputOSCache(this,this.configure.getOutputCacheSet().getProperties());
		this.application.setAttribute(HttpRuntime.APP_ATTR_OUTPUTCACHE, this.outputcache);
	}
	
	private Configure configure;
	private String requestEncoding;
	private String responseEncdoing;
	private String responseHeaderEncoding;
	//private String[] urlpatterns; 
	//private String urlpattern;
	//private String contrlJar;
	private String contrlPkg;
	private String templateRoot;
	private String igpath;
	private String applicationRoot;
	//private Properties oscacheProperties;
	//private Properties outputcacheProperties;
	private int uriSubIndex;
	private HttpApplication application;
	//private VelocityEngine vtlEngine;
	private TemplateEngine templateEngine;
	private OSCache oscache;
	private OutputOSCache outputcache;
	private Map<String,String> clstable;
	//private InterceptorInfo intceptorInfo;
	private final ConcurrentHashMap<String,ActionWrapper> acwsmap
	=new ConcurrentHashMap<String,ActionWrapper>();
	/*private final ConcurrentHashMap<String, ActionGroupInfo> acgpsmap
	=new ConcurrentHashMap<String, ActionGroupInfo>();                    
	private final ConcurrentHashMap<String,ActionInfo> acsmap
	=new ConcurrentHashMap<String,ActionInfo>();*/
	
	
	/*public InterceptorInfo getInterceptorInfo(){
		return this.intceptorInfo;
	}*/
	public Configure getConfigure(){
		return this.configure;
	}
	public HttpApplication getApplication(){
		return this.application;
	}
	public String getApplicationRoot(){
		return this.applicationRoot;
	}
	public OSCache getCache(){
		return this.oscache;
	}
	
	public String getRequestEncoding(){
		return this.requestEncoding;
	}
	public String getResponseEncoding(){
		return this.responseEncdoing;
	}
	public String getResponseHeaderEncoding(){
		return this.responseHeaderEncoding;
	}
	/*public String[] getUrlPatterns(){
		return this.urlpatterns;
	}*/
	/*public String getUrlPattern(){
		return this.urlpattern;
	}*/
	/*public String getContrlJar(){
		return this.contrlJar;
	}*/
	public String getContrlPkg(){
		return this.contrlPkg;
	}
	public String getVmRoot(){
		return this.templateRoot;
	}
	public String getTemplateRoot(){
		return this.templateRoot;
	}
	public String getUrlPathIg(){
		return this.igpath;
	}
	
	public String mapPath(String path){
		return this.application.getRealPath(path);
	}
	public String getEffectivePath(String uri){
		String effpath=null;
		int a=uri.lastIndexOf('.');
		if(this.uriSubIndex==0&&a==0)
			effpath=uri;
		if(a>0){
			effpath= uri.substring(uriSubIndex,a);
		}
		else{
			effpath= uri.substring(uriSubIndex);
		}
		effpath=effpath.replace("///", "/").replace("//", "/");
		return effpath;
	}
	
	/*private String convert2ClassName(String effPath){
		String nameIg;
		if(effPath.startsWith("/__")){
			nameIg="com.li.myweb."+effPath.substring(1);
		}
		else{
			if(this.contrlPkg!=null&&!this.contrlPkg.isEmpty())
				nameIg=this.contrlPkg+effPath.replace('/', '.');
			else
				nameIg=effPath.substring(1).replace('/', '.');
			nameIg=this.findClassNameIg(effPath.substring(1).replace('/', '.'));
		}
		return nameIg.toLowerCase();
	}*/
	/*public String convert2TemplatePath(String effPath){
		String path=effPath.substring(1);//+".vm";
		if(!HttpRuntime.PATHSEPARATOR.equals("/")){
			path=path.replace("/", HttpRuntime.PATHSEPARATOR);
		}
		path=this.templateEngine.getPathWithExtention(path);
		return path;
	}*/
	public String findTemplate(Method method){
		String tmpath0=method.getDeclaringClass().getName().toLowerCase();
		/*if(this.contrlPkg!=null&&this.contrlPkg.length()>0){
			tmpath0=tmpath0.substring(this.contrlPkg.length()+1).replace('.', File.separatorChar);
		}*/
		FileFilter ff=new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory()&&!arg0.isHidden();
			}};
		int a=-1;
		File[] files=new File(this.templateRoot).listFiles(ff);
		if(files!=null){
			for(File f:files){
				String fn=f.getName().toLowerCase();
				a=tmpath0.indexOf(fn+".");
				if(a>-1){
					if(a>0){
						if(tmpath0.charAt(a-1)!='.'){
							a=-1;
							continue;
						}
					}
					break;
				}
			}
		}
		if(a>-1){
			tmpath0=tmpath0.substring(a).replace('.', File.separatorChar);
		}
		else{
			tmpath0=method.getDeclaringClass().getSimpleName().toLowerCase();
		}
		String tmpath;
		//类名.方法名.模板后缀
		tmpath=tmpath0+"."+method.getName().toLowerCase();
		tmpath=this.templateEngine.getPathWithExtention(tmpath);
		//类名.模板后缀
		if(tmpath==null){
			tmpath=tmpath0;
			tmpath=this.templateEngine.getPathWithExtention(tmpath);
		}
		return tmpath;
	}
	/*@SuppressWarnings("unused")
	private String findRealName(String nameIg){
		String realName=null;
		
		String jar=null;
		
		if(nameIg.startsWith("com.li.myweb")){
			jar=this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			try {
				jar=URLDecoder.decode(jar,HttpRuntime.SYSTEMENCODING);
			} catch (UnsupportedEncodingException e) {
				
			}
			jar=jar.substring(1);
		}else{
			
			if(this.contrlJar!=null&&!this.contrlJar.isEmpty()){
	        	jar=this.contrlJar;
	        	if(jar.indexOf("/")==-1&&jar.indexOf(HttpRuntime.PATHSEPARATOR)==-1){
	        		String lib=this.mapPath("/WEB-INF/lib/");
	        		if(!lib.endsWith(HttpRuntime.PATHSEPARATOR)){
	        			lib+=HttpRuntime.PATHSEPARATOR;
	        		}
	        		jar=lib+jar;
	        	}
			}
		}
		if(jar!=null){
			realName=Utils.matchNameFromJAR(nameIg, jar);
		}
		
		if(realName==null){
			//String dir=Thread.currentThread().getContextClassLoader().getResource("/").getPath();
			String dir=this.mapPath("/WEB-INF/classes");
			realName=Utils.matchNameFromDirectory(nameIg, dir);
		}
		return realName;
	}*/
	private void scan(){
		File file;
		HashMap<String,String> map=new HashMap<String,String>();
		//扫描lib目录下的jar
		String lib=this.mapPath("/WEB-INF/lib/");
		file=new File(lib);
		if (file.exists()) {
			File[] list = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isFile()
							&& f.getName().toLowerCase().endsWith(".jar"))
						return true;
					return false;
				}
			});
			if (list != null && list.length > 0) {
				for (File jar : list) {
					for (String clsName : Utils.listJarClasses(jar)) {
						String lclsName = clsName.toLowerCase();
						if (this.contrlPkg != null && !this.contrlPkg.isEmpty()
								&& !clsName.startsWith(contrlPkg))
							continue;
						if (map.containsKey(lclsName))
							continue;
						map.put(lclsName, clsName);
					}
				}
			}
		}
		//扫描classes目录下的类
		String path=this.mapPath("/WEB-INF/classes/");
		file=new File(path);
		if (file.exists()) {
			for (String clsName : Utils.listDirClasses(file)) {
				String lclsName = clsName.toLowerCase();
				if (this.contrlPkg != null && !this.contrlPkg.isEmpty()
						&& !clsName.startsWith(contrlPkg))
					continue;
				if (map.containsKey(lclsName))
					continue;
				map.put(lclsName, clsName);
			}
		}
		this.clstable=map;
		this.application.setAttribute(HttpRuntime.APP_CONTRLCLASSES, this.clstable);
	}
	
	public Template getTemplate(String name){
		//return this.vtlEngine.getTemplate(name);
		return this.templateEngine.get(name);
	}
	
	/*private Object[] getAcgAndAct(String acgPath,String actName) 
	throws DefParamsUnmatchException
	, ClassNotFoundException
	, NoSuchMethodException{
		Object[] result=new Object[3];
		
		String classNameIg=this.convert2ClassName(acgPath);
		int state=0;
		
		ActionGroupInfo acg=this.acgpsmap.get(classNameIg);
		if(acg==null){
			Class<?> implClass=null;
			//String className=this.findRealName(classNameIg);
			String className=this.clstable.get(classNameIg);
			if(className==null)
				throw new ClassNotFoundException(className);
			ClassLoader loader=Thread.currentThread().getContextClassLoader();
			if(className==null||(implClass=Class.forName(className,true,loader))==null)
				throw new ClassNotFoundException(className);
			acg=new ActionGroupInfo(implClass);
			this.acgpsmap.putIfAbsent(classNameIg, acg);
			
			state|=1;
		}
		result[0]=acg;
		String key2=classNameIg+"@"+actName;
		ActionInfo act=this.acsmap.get(key2);
		if(act==null){
			Method method=Utils.lookupMethodIg(acg.impl,ActionGroupImpl.class,actName);
			if(method==null){
				throw new NoSuchMethodException();
			}
			act=new ActionInfo(method);
			this.acsmap.put(key2, act);
			
			state|=2;
		}
		result[1]=act;
		result[2]=state;
		return result;
	}
	*/
	public ActionWrapper getAction0(String clsname,String methodname) 
	throws DefParamsUnmatchException, ClassNotFoundException, NoSuchMethodException{
		return this.getAction0(clsname,methodname,null);
	}
	private String findClassNameIg(String input){
		String clsname=null;
		if(this.contrlPkg==null||this.contrlPkg.isEmpty()){
			String input2="."+input;
			for(String itm:this.clstable.keySet()){
				if(itm.equals(input)||itm.endsWith(input2)){
					if(clsname!=null){
						throw new RuntimeException(String.format("无法区分的Controller,%s与%s，通过设置/WEB-INF/stormweb.properties文件中的stormweb.mvc.pkg=<控制器包名>进行解决，或者更改当前类名或包名。",clsname,itm));
					}
					clsname=itm;
				}
			}
		}
		else{
			String input2=(this.contrlPkg+"."+input).toLowerCase();
			clsname=this.clstable.get(input2);
		}
		return clsname;
	}
	public ActionWrapper getAction0(String clsname,String methodname,ActionWrapper child) 
	throws DefParamsUnmatchException, ClassNotFoundException, NoSuchMethodException{
		String key=clsname+"@"+methodname;
		ActionWrapper acw=acwsmap.get(key);
		if(acw!=null)
			return acw;
		ActionGroupInfo acg;
		ActionInfo act;
		Class<?> implClass=null;
		String className=this.clstable.get(clsname);
		if(className==null)
			throw new ClassNotFoundException(className);
		ClassLoader loader=Thread.currentThread().getContextClassLoader();
		if(className==null||(implClass=Class.forName(className,true,loader))==null)
			throw new ClassNotFoundException(className);
		acg=new ActionGroupInfo(implClass);
		Method method=Utils.lookupMethodIg(implClass,ActionGroupImpl.class,methodname);
		if(method==null){
			throw new NoSuchMethodException();
		}
		act=new ActionInfo(method);
		
		OutputCache oc_g=acg.outputCache;
		OutputCache oc_a=act.outputCache;
		boolean oc_enable=false;
		int oc_periods=0;
		int oc_deps=0;
		boolean oc_cctrl=false;
		if(oc_g!=null||oc_a!=null){
			if(oc_g!=null){
				if(oc_enable=oc_g.enable()){
					oc_periods=oc_g.periods();
					oc_deps=oc_g.dependTypes();
					oc_cctrl=oc_g.enableCacheControl();
				}
			}
			if(oc_a!=null){
				if(oc_enable=oc_a.enable()){
					oc_periods=oc_a.periods();
					oc_deps=oc_a.dependTypes();
					oc_cctrl=oc_a.enableCacheControl();
				}
			}
			/*if(enable){
				this.outputcache.register(implLocPath,actionName,oc_periods,oc_deps,oc_cctrl);
			}*/
		}
		
		Layout groupLayout=acg.layout;
		Layout actLayout=act.layout;
		boolean outByTmpl=true;
		String tempath=null;
		
		
		if(groupLayout!=null){
			outByTmpl=groupLayout.isNeedVm()&groupLayout.templateOut();
			if(outByTmpl&&groupLayout.template()!=null&&!groupLayout.template().isEmpty()){
				tempath=groupLayout.template();
			}
		}
		if(actLayout!=null){
			outByTmpl=actLayout.isNeedVm()&actLayout.templateOut();
			if(outByTmpl&&actLayout.template()!=null&&!actLayout.template().isEmpty()){
				tempath=actLayout.template();
			}
		}
		if(outByTmpl){
			if(tempath==null){
				tempath=this.findTemplate(act.method);
			}
			/*if(tempath==null){
				throw new TemplateException("未能找到action对应的输出模板，如果action不采用模板输出，请设置Layout注解的templateOut属性为false。Action:"+act.method.getName());
			}*/
		}
		
		Master groupMaster=acg.master;
		Master actMaster=act.master;
		
		String mstref=null;
		String mstctx=null;
		if(actMaster!=null){
			mstref=actMaster.ref();
			mstctx=actMaster.label();
		}
		else if(groupMaster!=null){
			mstref=groupMaster.ref();
			mstctx=groupMaster.label();
		}
		
		if(child==null){
			acw=new ActionWrapper(acg.impl,act.method
				,acg.modifiedEnable
				,acg.etagEnable
				,oc_enable
				,oc_periods
				,oc_deps
				,oc_cctrl
				,tempath
				,act.paramNames
				,act.paramTypes
				,act.allows);
		}
		else{
			acw=new ActionWrapper(acg.impl,act.method,tempath,act.paramNames,act.paramTypes);
		}
		//如果有Master
		ActionWrapper macw=null;
		if(mstref!=null){
			int a=mstref.lastIndexOf('@');
			if(a==-1)
				throw new RuntimeException("母版页引用格式错误：指定@Master注解时，ref格式为：类名@方法名。");
			String mclsname=mstref.substring(0,a);
			String mmethodname=mstref.substring(a+1);
			if(mclsname.equalsIgnoreCase(acg.impl.getName()))
				throw new RuntimeException(String.format("Action输出的master所在的类不能与Action所在的类是同一个类，action:%s.%s,master:%s",acg.impl.getName(),act.method.getName(),mstref));
			macw=this.getAction0(mclsname, mmethodname);
		}
		if(macw!=null){
			acw.setMasterActionWrapper(macw);
			acw.setMasterContextLabel(mstctx);
		}
		ActionWrapper acw2=this.acwsmap.putIfAbsent(key, acw);
		if(acw2!=null){
			acw=acw2;
		}
		return acw;
	}
	public ActionWrapper getAction0(HttpRequest request) throws DefParamsUnmatchException{
		String effecPath=this.getEffectivePath(request.getRequestURI()).toLowerCase();
		String actionName=request.getAction().toLowerCase();
		int lx=effecPath.lastIndexOf("/");
		ActionWrapper acw=null;
		String clsName=effecPath.substring(1).replace('/', '.');
		try{
			String tmp=this.findClassNameIg(clsName);//this.convert2ClassName(effecPath);
			if(tmp==null)
				throw new ClassNotFoundException(clsName);
			clsName=tmp.toLowerCase();
			acw=this.getAction0(clsName, actionName);
			return acw;
		}
		catch(ClassNotFoundException e){
			//如果已经显式指明了执行的Action,或者路径根本不带参数,直接抛出出现的异常
			if(!request.isDefaultAction()||lx==0){
				throw new RuntimeException(e);
			}
		}
		catch(NoSuchMethodException e){
			//如果已经显式指明了执行的Action,或者路径根本不带参数,直接抛出出现的异常
			if(!request.isDefaultAction()||lx==0){
				throw new RuntimeException(e);
			}
		}
		//否则考虑是否是路径形式的参数：ActionImpl/Action
		lx=clsName.lastIndexOf('.');
		String clsName2=clsName.substring(0,lx);
		String actionName2=clsName.substring(lx+1);
		try{
			String tmp=this.findClassNameIg(clsName2);
			if(tmp==null)
				throw new ClassNotFoundException(clsName2);
			clsName2=tmp.toLowerCase();
			acw=this.getAction0(clsName2, actionName2);
		}
		catch(Exception e){
			if(e instanceof DefParamsUnmatchException)
				throw (DefParamsUnmatchException)e;
			throw new RuntimeException(e);
		}
		return acw;
	}
	public void RegisterActionOutCache(HttpRequest req,ActionWrapper acw){
		this.outputcache.register(req, acw.cachePeriods, acw.cacheDeps, acw.cacheControlled);
	}
	/*public ActionWrapper getAction(HttpRequest request) 
	throws DefParamsUnmatchException
	, ClassNotFoundException
	, NoSuchMethodException
	{
		String effecPath=this.getEffectivePath(request.getRequestURI()).toLowerCase();
		//规避路径中可能出现的“//”风险。
		//effecPath=effecPath.replace("///", "/").replace("//", "/");
		String actionName=request.getAction().toLowerCase();
		String acwLocKey=null;   
		String implLocPath=null;  
		ActionWrapper acw=null;
		
		//当前路径:方法名
		acwLocKey=effecPath+":"+actionName;
		acw=this.acwsmap.get(acwLocKey);
		//如果找到Action，直接返回
		if(acw!=null)
			return acw;
		//如果没找到且是默认方法，判断是否是路径形式的参数：ActionImpl/Action
		int lx=0;
		if(request.isDefaultAction()&&(lx=effecPath.lastIndexOf("/"))>0){
			acwLocKey=effecPath.substring(0,lx)+":"+effecPath.substring(lx+1);
			acw=this.acwsmap.get(acwLocKey);
			//如果找到Action，直接返回
			if(acw!=null)
				return acw;
		}
		//确认了Action还没有被加载过
		Object[] gas=null;
		implLocPath=effecPath;
		Exception ex=null;
		try
		{
			gas=this.getAcgAndAct(implLocPath, actionName);
			acwLocKey=effecPath+":"+actionName;
		}
		catch(Exception e){
			//如果已经显式指明了执行的Action,或者路径根本不带参数,直接抛出出现的异常
			//否则考虑是否是路径形式的参数：ActionImpl/Action
			if(!request.isDefaultAction()||lx==0){
				if(e instanceof DefParamsUnmatchException)
					throw (DefParamsUnmatchException)e;
				if(e instanceof NoSuchMethodException)
					throw (NoSuchMethodException)e;
				if(e instanceof ClassNotFoundException)
					throw (ClassNotFoundException)e;
			}
			ex=e;
		}
		if(ex!=null){
			//路径形式的参数：ActionImpl/Action
			implLocPath=effecPath.substring(0,lx);
			actionName=effecPath.substring(lx+1);
			try
			{
				gas=this.getAcgAndAct(implLocPath, actionName);
				acwLocKey=effecPath.substring(0,lx)+":"+effecPath.substring(lx+1);
			}
			catch(Exception ex2){
				//如果上轮查找抛出的是DefParamsUnmatchException，优先认为是上轮异常
				if(ex instanceof DefParamsUnmatchException)
					throw (DefParamsUnmatchException)ex;
				if(ex2 instanceof DefParamsUnmatchException)
					throw (DefParamsUnmatchException)ex2;
				if(ex2 instanceof NoSuchMethodException)
					throw (NoSuchMethodException)ex2;
				if(ex2 instanceof ClassNotFoundException)
					throw (ClassNotFoundException)ex2;
			}
		}
		
		
		
		int lx=0;
		boolean noActionParam=request.isDefaultAction()&&(lx=effecPath.lastIndexOf("/"))>0;
		if(noActionParam){
			acwLocKey=effecPath;
			acw=this.acwsmap.get(acwLocKey);
			if(acw!=null)
				return acw;
		}
		else{
			acwLocKey=effecPath+"/"+actionName;
			acw=this.acwsmap.get(acwLocKey);
			if(acw!=null)
				return acw;
		}
		if(noActionParam){
			implLocPath=effecPath.substring(0,lx);
			String actName2=effecPath.substring(lx+1);
			try
			{
				gas=this.getAcgAndAct(implLocPath, actName2);
				actionName=actName2;
			}
			catch(DefParamsUnmatchException e){
				throw e;
			}
			catch(NoSuchMethodException e){
				throw e;
			}
			catch(ClassNotFoundException e){
			}
		}
		
		if(gas==null){
			implLocPath=effecPath;
			gas=this.getAcgAndAct(implLocPath, actionName);
		}
		ActionGroupInfo acg=(ActionGroupInfo)gas[0];
		ActionInfo act=(ActionInfo)gas[1];
		int state=(Integer)gas[2];
		
		if((state&2)==2){
			
			OutputCache oc_g=acg.outputCache;
			OutputCache oc_a=act.outputCache;
			
			if(oc_g!=null||oc_a!=null){
				int oc_periods=0;
				int oc_deps=0;
				boolean oc_cctrl=false;
				boolean enable=false;
				if(oc_g!=null){
					if(enable=oc_g.enable()){
						oc_periods=oc_g.periods();
						oc_deps=oc_g.dependTypes();
						oc_cctrl=oc_g.enableCacheControl();
					}
				}
				if(oc_a!=null){
					if(enable=oc_a.enable()){
						oc_periods=oc_a.periods();
						oc_deps=oc_a.dependTypes();
						oc_cctrl=oc_a.enableCacheControl();
					}
				}
				if(enable){
					//this.outputcache.register(request, oc_periods, oc_deps, oc_cctrl);
					this.outputcache.register(implLocPath,actionName,oc_periods,oc_deps,oc_cctrl);
				}
			}
		}
		Layout groupLayout=acg.layout;
		Layout actLayout=act.layout;
		boolean outByTmpl=true;
		String tempath=null;
		
		
		if(groupLayout!=null){
			outByTmpl=groupLayout.isNeedVm();
			if(outByTmpl&&groupLayout.template()!=null&&!groupLayout.template().isEmpty()){
				tempath=groupLayout.template();
			}
		}
		if(actLayout!=null){
			outByTmpl=actLayout.isNeedVm();
			if(outByTmpl&&actLayout.template()!=null&&!actLayout.template().isEmpty()){
				tempath=actLayout.template();
			}
		}
		//Template template=null;
		
		if(outByTmpl){
			if(tempath==null){
				//tempath=this.convert2TemplatePath(implLocPath);
				tempath=this.findTemplate(act.method);
			}
			if(tempath==null){
				throw new TemplateException("未能找到action对应的输出模板，如果action不采用模板输出，请设置Layout注解的templateOut属性为false");
			}
			
			
			try{
				template=this.getTemplate(tempath);
			}
			catch(ResourceNotFoundException e){
				throw new TemplateException(e);
			}
			catch(ParseErrorException e){
				throw new TemplateException(e);
			}
			
		}
		acw=new ActionWrapper(acg.impl,act.method
				,acg.modifiedEnable,acg.etagEnable
				//,template
				,tempath
				,act.paramNames,act.paramTypes,act.allows);
		this.acwsmap.putIfAbsent(acwLocKey, acw);
		return acw;
	}*/
	/*
	public ActionWrapper getAction(HttpRequest request) 
	throws ClassNotFoundException
	, NoSuchMethodException
	, DefParamsUnmatchException
	, TemplateException{
		String uri=request.getRequestURI().toLowerCase();
		String actionName=request.getAction();
		actionName=actionName.toLowerCase();
		
		String key1=uri+"@"+actionName;
		ActionWrapper acw=this.acwsmap.get(key1);
		if(acw!=null)
			return acw;
		if(request.isDefaultAction()&&request.method.equals(HttpMethod.HEAD)){
			acw=this.acwsmap.get(uri+"@doget");
			if(acw!=null)
				return acw;
		}
		String effecPath=this.getEffectivePath(uri);
		
		String classNameIg=this.convert2ClassName(effecPath);
		
		ActionGroupInfo acg=this.acgpsmap.get(classNameIg);
		
		if(acg==null){
			Class<?> implClass=null;
			String className=this.findRealName(classNameIg);
			
			if(className==null)
				throw new ClassNotFoundException(className);
			ClassLoader loader=Thread.currentThread().getContextClassLoader();
			
			if(className==null||(implClass=Class.forName(className,true,loader))==null)
				throw new ClassNotFoundException(className);
			acg=new ActionGroupInfo(implClass);
			this.acgpsmap.putIfAbsent(classNameIg, acg);
		}
		String key2=classNameIg+"@"+actionName;
		ActionInfo act=this.acsmap.get(key2);
		if(act==null){
			Method method=Utils.lookupMethodIg(acg.impl,ActionGroupImpl.class,actionName);
			if(method==null){
				throw new NoSuchMethodException();
			}
			act=new ActionInfo(method);
			
			OutputCache oc_g=acg.outputCache;
			OutputCache oc_a=act.outputCache;
			
			if(oc_g!=null||oc_a!=null){
				int oc_periods=0;
				int oc_deps=0;
				boolean oc_cctrl=false;
				boolean enable=false;
				if(oc_g!=null){
					if(enable=oc_g.enable()){
						oc_periods=oc_g.periods();
						oc_deps=oc_g.dependTypes();
						oc_cctrl=oc_g.enableCacheControl();
					}
				}
				if(oc_a!=null){
					if(enable=oc_a.enable()){
						oc_periods=oc_a.periods();
						oc_deps=oc_a.dependTypes();
						oc_cctrl=oc_a.enableCacheControl();
					}
				}
				if(enable){
					this.outputcache.register(request, oc_periods, oc_deps, oc_cctrl);
				}
			}
			this.acsmap.put(key2, act);
		}
		Layout groupLayout=acg.layout;
		Layout actLayout=act.layout;
		boolean outByTmpl=true;
		String tempath=null;
		
		
		if(groupLayout!=null){
			outByTmpl=groupLayout.isNeedVm();
			if(outByTmpl&&groupLayout.template()!=null&&!groupLayout.template().isEmpty()){
				tempath=groupLayout.template();
			}
		}
		if(actLayout!=null){
			outByTmpl=actLayout.isNeedVm();
			if(outByTmpl&&actLayout.template()!=null&&!actLayout.template().isEmpty()){
				tempath=actLayout.template();
			}
		}
		Template template=null;
		
		if(outByTmpl){
			if(tempath==null){
				tempath=this.convert2TemplatePath(effecPath);
			}
			
			try{
				template=this.getTemplate(tempath);
			}
			catch(ResourceNotFoundException e){
				throw new TemplateException(e);
			}
			catch(ParseErrorException e){
				throw new TemplateException(e);
			}
		}
		acw=new ActionWrapper(acg.impl,act.method
				,acg.modifiedEnable,acg.etagEnable
				,template,act.paramNames,act.paramTypes,act.allows);
		this.acwsmap.putIfAbsent(key1, acw);
		return acw;
	}
	*/
	public String urlEncode(String input){
		if(this.requestEncoding!=null)
			return HttpUtility.urlEncode(input, this.requestEncoding);
		else
			return HttpUtility.urlEncode(input,HttpRuntime.SYSTEMENCODING);
		
	}
	public String urlDecode(String input){
		if(this.requestEncoding!=null)
			return HttpUtility.urlDecode(input, this.requestEncoding);
		else
			return HttpUtility.urlDecode(input,HttpRuntime.SYSTEMENCODING);
		
	}
	public String htmlEncode(String input){
		return HttpUtility.htmlEncode(input);
	}
	public String htmlDecode(String input){
		return HttpUtility.htmlDecode(input);
	}
	public void destroy(){
		this.oscache.destroy();
		this.outputcache.destroy();
	}
}
