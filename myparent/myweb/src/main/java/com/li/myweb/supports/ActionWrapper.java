package com.li.myweb.supports;

import java.lang.reflect.Method;

import eagle.web.HttpMethod;

public class ActionWrapper {
	public ActionWrapper(Class<?> cls
			,Method method
			,boolean suprtLastModifiy
			,boolean suprtEtag
			,boolean cacheEnabled
			,int cachePeriods
			,int cachedDeps
			,boolean cachedControlled
			,String tmp
			,String[] pmNames
			,Class<?>[] pmTypes
			,int methodAllows){
		this.classi=cls;
		this.method=method;
		this.supportEtag=suprtEtag;
		this.supportLastModifiy=suprtLastModifiy;
		if(tmp!=null){
			//this.template=tmp;
			this.templateout=true;
			this.templatePath=tmp;
		}
		else{
			this.templateout=false;
			//this.template=null;
			this.templatePath=null;
		}
		this.paramNames=pmNames;
		this.paramTypes=pmTypes;
		this.mallows=methodAllows;
		this.cacheEnabled=cacheEnabled;
		this.cachePeriods=cachePeriods;
		this.cacheDeps=cachedDeps;
		this.cacheControlled=cachedControlled;
		//this.isMaster=true;
	}
	public ActionWrapper(Class<?> cls,Method method,String tmp
			,String[] pmNames,Class<?>[] pmTypes){
		this(cls,method,false,false,false,0,0,false,tmp,pmNames,pmTypes,0);
		//this.isMaster=false;
	}
	public final Class<?> classi;
	public final Method method;
	public final boolean supportLastModifiy;
	public final boolean supportEtag;
	public final boolean templateout;
	public final String templatePath;
	public final String[] paramNames;
	public final Class<?>[] paramTypes;
	public final boolean cacheEnabled;
	public final int cachePeriods;
	public final int cacheDeps;
	public final boolean cacheControlled;
	private final int mallows;
	private ActionWrapper masterActionWrapper;
	private String masterContextLabel;
	//private boolean isMaster;
	private boolean useAsMaster;
	
	public boolean useAsMaster(){
		return this.useAsMaster;
	}
	
	public boolean allow(String httpMethod){
		int c=HttpMethod.getCode(httpMethod);
		return (c&mallows)==c;
	}
	public void setMasterActionWrapper(ActionWrapper masterActionWrapper) {
		this.masterActionWrapper = masterActionWrapper;
		this.masterActionWrapper.useAsMaster=true;
	}
	public ActionWrapper getMasterActionWrapper() {
		return masterActionWrapper;
	}
	public void setMasterContextLabel(String masterContextLabel) {
		this.masterContextLabel = masterContextLabel;
	}
	public String getMasterContextLabel() {
		return masterContextLabel;
	}
	/*public boolean IsMaster(){
		return this.isMaster;
	}*/
}
