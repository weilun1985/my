package com.li.myweb;

public class HttpRuntime {
	static{
		//Package pkg=HttpRuntime.class.getPackage();
		//VERSION=pkg.getImplementationTitle()+" VER "+pkg.getImplementationVersion()+"("+pkg.getImplementationVendor()+")";
		//VERSION="StormWeb,VER-1.9.5beta2,(STARTRACE/LCZ)";
	}
	
	public static final String PATHSEPARATOR=System.getProperty("file.separator");    //路径分隔符号
	public static final String SYSTEMENCODING=System.getProperty("file.encoding");    //系统文件编码
	public static final String LANGUAGE=System.getProperty("user.language");          //语言
	public static final String REGION=System.getProperty("user.region");              //地域
	//public static final String VERSION;
	public static final int OUTPUTCACHEPERIODS=300;
	
	public final static String FRAME_CONFIG_PAREMNAME="com.li.myweb";
	public final static String APP_ATTR_SERVER="Server";
	public final static String APP_ATTR_APP="Application";
	public final static String APP_ATTR_OSCACHE="oscache";
	public final static String APP_ATTR_OUTPUTCACHE="outputCache";
	public final static String APP_TEPMLATEENGINE="TemplateEngie";
	public final static String APP_STARTTIME="app_startTime";
	public final static String APP_DISPLAYINFO="app_displayInfo";
	public final static String APP_ERROR="app_lastError";
	public final static String APP_CONFIGS="app_configs";
	public final static String APP_CONTRLCLASSES="app_contrl_classes";
	public static final String APP_CONTRLPACKAGE = "app_contrl_package";
	public static final String APP_SERVLETDYNAMIC = "app_servletdynamic";
	
	
	public final static String REQATTR_ENTERTIME="enterTime";
	public final static String REQATTR_ENTERTIME_INTERCEPTOR="enterTime@interceptor";
	public final static String REQATTR_ENTERTIME_OUTPUTCACHE="enterTime@outputCache";
	public final static String REQATTR_ENTERTIME_HTTPSERVLET="enterTime@httpServlet";
	public final static String REQATTR_EXECCURRENT="exec_current";
	
	
}
