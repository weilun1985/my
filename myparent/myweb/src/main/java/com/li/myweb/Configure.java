package com.li.myweb;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
//import java.util.ArrayList;
import java.util.Properties;

public class Configure {
	public static final String DEFAULT_FILE="myweb-default.properties";
	public static final String VARIABLES_PREFIX="myweb.variables.";
	//编码方式
	public class Encode{
		public static final String REQUEST="stormweb.enc.req";
		public static final String RESPONSE="stormweb.enc.resp";
		public static final String RESPONSE_HEADER="stormweb.enc.resph";
		public Encode(){}
		private String reqenc="utf-8";
		private String respenc="utf-8";
		private String resphenc="utf-8";
		
		public String getReqEnc(){
			return reqenc;
		}
		public String getRespEnc(){
			return respenc;
		}
		public String getResphEnc(){
			return resphenc;
		}
		public void config(Properties properties){
			String vstr;
			vstr=properties.getProperty(REQUEST);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.reqenc=vstr;
			}
			vstr=properties.getProperty(RESPONSE);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.respenc=vstr;
			}
			vstr=properties.getProperty(RESPONSE_HEADER);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.resphenc=vstr;
			}
		}
		public void config(String cfstr){
			if(cfstr==null||cfstr.length()==0)
				return;
			String[] items=cfstr.split(",");
			for(String itm:items){
				String[] kv=itm.split("=",2);
				if(kv.length<2)
					continue;
				if(kv[0].equals("req"))
					this.reqenc=kv[1];
				else if(kv[0].equals("resp"))
					this.respenc=kv[1];
				else if(kv[0].equals("resph"))
					this.resphenc=kv[1];
			}
		}
	}
	//MVC参数
	public class MVC{
		public static final String PACKAGE="stormweb.mvc.pkg";
		public static final String VIEW="stormweb.mvc.view";
		public static final String JARFILE="stormweb.mvc.jar";
		public static final String IGPATH="stormweb.mvc.igpath";
		public static final String URLPATTEN="stormweb.mvc.urlpatten";
		public MVC(){}
		
		//模板目录
		private String vmroot;//=HttpRuntime.PATHSEPARATOR+"views"+HttpRuntime.PATHSEPARATOR;
		//URL映射正则
		//private String urlpatten="*.do";
		private String[] urlpattens=new String[]{"*.do"};
		/*//控制器所在JAR名称
		private String ctljar;*/
		//控制器所在包名
		private String ctlpkg;
		//路径识别时忽略的路径
		private String igpath;
		
//		public String getVMRoot(){
//			return vmroot;
//		}
		/*public String getUrlPatten(){
			return urlpatten;
		}*/
		public String[] getUrlPattens(){
			return this.urlpattens;
		}
		/*public String[] getUrlPattens(){
			String[] urlpatterns=null;
			if(this.urlpatten!=null){
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
			}
			return urlpatterns;
		}*/
		/*public String getControllerJAR(){
			return ctljar;
		}*/
		public String getControllerPKG(){
			return ctlpkg;
		}
		public String getIgPath(){
			return igpath;
		}
		public void config(Properties properties){
			String vstr;
			vstr=properties.getProperty(JARFILE);
			/*if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.ctljar=vstr;
			}*/
			vstr=properties.getProperty(PACKAGE);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.ctlpkg=vstr;
			}
			vstr=properties.getProperty(VIEW);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.vmroot=vstr;
			}
			vstr=properties.getProperty(IGPATH);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.igpath=vstr;
			}
			vstr=properties.getProperty(URLPATTEN);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				//this.urlpatten=vstr;
				vstr=vstr.toLowerCase();
				this.urlpattens=vstr.split(",");
			}
		}
		public void config(String cfstr){
			if(cfstr==null||cfstr.length()==0)
				return;
			String[] items=cfstr.split(",");
			for(String itm:items){
				String[] kv=itm.split("=",2);
				if(kv.length<2)
					continue;
				/*if(kv[0].equals("jar"))
					this.ctljar=kv[1];*/
				else if(kv[0].equals("pkg")&&(kv[1]!=null&&(kv[1]=kv[1].trim()).length()>0))
					this.ctlpkg=kv[1];
				else if(kv[0].equals("vmroot")&&(kv[1]!=null&&(kv[1]=kv[1].trim()).length()>0)){
					vmroot=kv[1];
				}
				else if(kv[0].equals("igpath")&&(kv[1]!=null&&(kv[1]=kv[1].trim()).length()>0))
					igpath=kv[1];
				else if(kv[0].equals("urlpatten")&&(kv[1]!=null&&(kv[1]=kv[1].trim().toLowerCase()).length()>0)){
					urlpattens=kv[1].split(",");
				}
			}
		}
	}
	//Cache配置
	public class Cache{
		private Properties properties;
		public Properties getProperties(){
			return this.properties;
		}
		public void config(String cfstr){
			if(this.properties==null){
				this.properties=new Properties();
			}
			if(cfstr==null||cfstr.length()==0)
				return;
			String[] items=cfstr.split(",");
			for(String itm:items){
				String[] kv=itm.split("=",2);
				if(kv.length<2)
					continue;
				properties.setProperty("cache."+kv[0], kv[1]);
			}
		}
		public void config(Properties pros){
			if(this.properties==null){
				this.properties=pros;
			}else{
				for(Entry<Object,Object> entry:pros.entrySet()){
					String key=entry.getKey().toString();
					if(!key.startsWith("cache."))
						continue;
					String value=entry.getValue()==null?null:entry.getValue().toString().trim();
					if(value==null||value.length()==0)
						continue;
					if(this.properties.containsKey(key))
						this.properties.setProperty(key, value);
					else
						this.properties.put(key, value);
				}
			}
		}
	}
	//上传配置
	public class Upload{
		public static final String MAXMEMSIZE="stormweb.upload.maxmemsize";
		public static final String MAXFILESIZE="stormweb.upload.maxfilesize";
		public static final String TEMPDIR="stormweb.upload.tempdir";
		public void config(String cfstr){
			if(cfstr==null||cfstr.length()==0)
				return;
			String[] items=cfstr.split(",");
			for(String itm:items){
				String[] kv=itm.split("=",2);
				if(kv.length<2)
					continue;
				if(kv[0].equals("maxmemsize"))
					this.maxMemSize=Integer.parseInt(kv[1]);
				if(kv[0].equals("tempdir"))
					this.tempDir=kv[1];
				if(kv[0].equals("filesizemax"))
					this.fileSizeMax=Integer.parseInt(kv[1]);
			}
		}
		public void config(Properties properties){
			String vstr;
			vstr=properties.getProperty(MAXMEMSIZE);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.maxMemSize=Integer.parseInt(vstr);
			}
			vstr=properties.getProperty(TEMPDIR);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.tempDir=vstr;
			}
			vstr=properties.getProperty(MAXFILESIZE);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.fileSizeMax=Integer.parseInt(vstr);
			}
		}
		//最大使用内存数
		private long maxMemSize=2097152;
		//临时目录
		private String tempDir;
		//允许上传的单个文件最大大小
		private long fileSizeMax=2097152;
		public long getMaxMemSize(){
			return this.maxMemSize;
		}
		public String tempDir(){
			return this.tempDir;
		}
		public long getFileSizeMax(){
			return this.fileSizeMax;
		}
	}
	public class Intercept{
		public static final String WHITELIST="stormweb.security.whitelist";
		public static final String BLACKLIST="stormweb.security.blacklist";
		public static final String AUTOINTERCEPT="stormweb.security.autoIntercept";
		public static final String AUTOINTERCEPT_FREQ="stormweb.security.autoIntercept.frequency";
		public static final String AUTOINTERCEPT_TIME="stormweb.security.autoIntercept.time";
		
		public void config(String cfgStr){
			if(cfgStr==null||cfgStr.length()==0)
				return;
			String[] items=cfgStr.split(",");
			for(String itm:items){
				String[] kv=itm.split("=",2);
				if(kv.length<2)
					continue;
				if(kv[0].equals("blacklist")&&!(kv[1]=kv[1].trim()).isEmpty())
					this.blackListFile=kv[1];
				else if(kv[0].equals("whitelist")&&!(kv[1]=kv[1].trim()).isEmpty())
					this.whiteListFile=kv[1];
				else if(kv[0].equals("auto-forbidexp")&&!(kv[1]=kv[1].trim()).isEmpty()){
					this.autoForbidExp=Integer.parseInt(kv[1]);
				}
				else if(kv[0].equals("auto-accesslim")&&!(kv[1]=kv[1].trim()).isEmpty()){
					this.autoAccessLimit=Integer.parseInt(kv[1]);
				}
				else if(kv[0].equals("auto")&&!(kv[1]=kv[1].trim()).isEmpty()){
					this.autoIntercept=Boolean.parseBoolean(kv[1]);
				}
			}
		}
		public void config(Properties properties){
			String vstr;
			vstr=properties.getProperty(WHITELIST);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.whiteListFile=vstr;
			}
			vstr=properties.getProperty(BLACKLIST);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.blackListFile=vstr;
			}
			vstr=properties.getProperty(AUTOINTERCEPT);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.autoIntercept=Boolean.parseBoolean(vstr);
			}
			vstr=properties.getProperty(AUTOINTERCEPT_FREQ);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.autoAccessLimit=Integer.parseInt(vstr);
			}
			vstr=properties.getProperty(AUTOINTERCEPT_TIME);
			if(vstr!=null&&(vstr=vstr.trim()).length()>0){
				this.autoForbidExp=Integer.parseInt(vstr);
			}
		}
		private boolean autoIntercept=false;                    //是否开启自动屏蔽功能
		private int autoForbidExp=15;                        //自动屏蔽功能的黑名单有效期（分钟）
		private int autoAccessLimit=60;                      //访问数阀值（平均每分钟）
		private String blackListFile;                     //预订的黑名单文件路径
		private String whiteListFile;					 //预订的白名单文件路径
		
		public boolean isAutoIntercept(){
			return this.autoIntercept;
		}
		public int getAutoForbidExp(){
			return this.autoForbidExp;
		}
		public int getAutoAccessLimit(){
			return this.autoAccessLimit;
		}
		public String getBlackList(){
			return this.blackListFile;
		}
		public String getWhiteList(){
			return this.whiteListFile;
		}
		public boolean enabled(){
			return this.whiteListFile!=null||this.blackListFile!=null||this.autoIntercept==true;
		}
	}
	public Configure() throws IOException{
		InputStream stream=null;
		this.properties0=new Properties();
		try
		{
			stream=Configure.class.getClassLoader().getResourceAsStream(DEFAULT_FILE);
			//Properties properties=new Properties(); 
			properties0.load(stream);
			this.encode=new Encode();
			this.encode.config(properties0);
			this.mvc=new MVC();
			this.mvc.config(properties0);
			this.objCache=this.outputCache=new Cache();
			this.objCache.config(properties0);
			this.intercept=new Intercept();
			this.intercept.config(properties0);
			this.upload=new Upload();
			this.upload.config(properties0);
		}
		finally{
			if(stream!=null)
				stream.close();
		}
		this.variables=new HashMap<String,Object>();
		this.variables.putAll(System.getenv());
	}
	/*public void loadFile(String path) throws IOException{
		java.util.Stack<Properties> stack=new java.util.Stack<Properties>();
		while(true){
			Properties pros=new Properties();
			FileInputStream in=null;
			try
			{
				in=new FileInputStream(path);
				pros.load(in);
			}finally{
				if(in!=null)
					in.close();
			}
			stack.push(pros);
			Object parentObj=pros.get("config.parent");
			if(parentObj==null||(path=parentObj.toString().trim()).length()==0)
				break;
		}
		while(stack.size()>0){
			Properties pros=stack.pop();
			this.encode.config(pros);
			this.mvc.config(pros);
			this.objCache.config(pros);
			this.intercept.config(pros);
			this.upload.config(pros);
			this.getVariables(pros);
		}
	}*/
	public void loadFile(String path) throws IOException{
		java.util.Stack<Properties> stack=new java.util.Stack<Properties>();
		while(true){
			Properties pros=new Properties();
			FileInputStream in=null;
			try
			{
				in=new FileInputStream(path);
				pros.load(in);
			}finally{
				if(in!=null)
					in.close();
			}
			stack.push(pros);
			Object parentObj=pros.get("config.parent");
			if(parentObj==null||(path=parentObj.toString().trim()).length()==0)
				break;
		}
		while(stack.size()>0){
			Properties pros=stack.pop();
			this.properties0.putAll(pros);
		}
		this.encode.config(this.properties0);
		this.mvc.config(this.properties0);
		this.objCache.config(this.properties0);
		this.intercept.config(this.properties0);
		this.upload.config(this.properties0);
		this.getVariables(this.properties0);
	}
	public void loadConfigStr(String configStr){
		if(configStr!=null){
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
					this.encode.config(nv[1]);
				}
				else if(groupName.equals("mvc")&&!(nv[1]=nv[1].trim()).isEmpty()){
					this.mvc.config(nv[1]);
				}
				else if(groupName.equals("oscache")&&!(nv[1]=nv[1].trim()).isEmpty()){
					this.objCache.config(nv[1]);
				}
				else if(groupName.equals("outputcache")&&!(nv[1]=nv[1].trim()).isEmpty()){
					this.outputCache.config(nv[1]);
				}
				else if(groupName.equals("holdup")&&!(nv[1]=nv[1].trim()).isEmpty()){
					this.intercept.config(nv[1]);
				}
				else if(groupName.equals("upload")&&!(nv[1]=nv[1].trim()).isEmpty()){
					this.upload.config(nv[1]);
				}
			}
		}
	}
	private Encode encode;
	private MVC mvc;
	private Cache objCache;
	private Cache outputCache;
	private Upload upload;
	private Intercept intercept;
	private Map<String,Object> variables;
	private Properties properties0;
	
	//private Properties properties;
	private void getVariables(Properties pros){
		/*if(this.variables==null){
			this.variables=new HashMap<String,Object>();
			this.variables.putAll(System.getenv());
		}*/
		if(pros!=null){
			for(Entry<Object,Object> entry:pros.entrySet()){
				String k=entry.getKey().toString();
				Object v=entry.getValue();
				if(k.startsWith(VARIABLES_PREFIX)){
					this.variables.put(k, v);
				}
			}
		}
	}
	public Properties getProperties(){
		return this.properties0;
	}
	public Map<String,Object> getVariables(){
		return this.variables;
	}
	public Encode getEncodeSet(){
		return this.encode;
	}
	public MVC getMVCSet(){
		return this.mvc;
	}
	public Cache getObjCacheSet(){
		return this.objCache;
	}
	public Cache getOutputCacheSet(){
		return this.outputCache;
	}
	public Upload getUploadSet(){
		return this.upload;
	}
	public Intercept getInterceptSet(){
		return this.intercept;
	}
}
