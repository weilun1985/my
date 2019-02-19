package com.li.myweb;

import java.io.File;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
//import com.googlecode.httl.Engine;
import httl.Engine;
//import httl.Template;

import com.li.myweb.templates.HttlTemplate;
import com.li.myweb.templates.Template;
import com.li.myweb.templates.VtlTemplate;

public class TemplateEngine {
	/*public TemplateEngine(String approot,String tmproot,String outputenc){
		this.tmproot=tmproot;
		this.outputenc=outputenc;
		this.approot=approot;
		this.initVtlEngine();
		this.initHttlEngine();
	}*/
	public TemplateEngine(Properties pros,String tmproot,String outputenc){
		this.properties=pros;
		this.tmproot=tmproot;
		this.outputenc=outputenc;
		//public property
		if(this.outputenc!=null)
			this.properties.put("output.encoding", outputenc);
		//init template engines
		this.initVtlEngine();
		this.initHttlEngine();
	}
	//private String approot;
	private String tmproot;
	private String outputenc;
	private VelocityEngine vtlEngine;
	private Engine httlEngine;
	private Properties properties;
	private void initVtlEngine(){
		this.properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, this.tmproot);
		this.vtlEngine=new VelocityEngine();
		//this.vtlEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, this.tmproot);  
		//this.vtlEngine.setProperty(VelocityEngine.INPUT_ENCODING, "UTF-8");				
		//this.vtlEngine.setProperty(VelocityEngine.INPUT_ENCODING, "UTF-8");
		//if(this.outputenc!=null)
		//	this.vtlEngine.setProperty(VelocityEngine.OUTPUT_ENCODING, this.outputenc);				
		this.vtlEngine.init(this.properties);
	}
	private void initHttlEngine(){
//		//Properties properties=new Properties();
//		//properties.put(Engine.TEMPLATE_DIRECTORY, this.tmproot);
//		this.properties.put(Engine.TEMPLATE_DIRECTORY, this.tmproot);
//		/*properties.put(Engine.INPUT_ENCODING, "UTF-8");
//		properties.put(Engine.LOADER, "com.googlecode.httl.support.loaders.FileLoader");
//		properties.put(Engine.PARSER, "com.googlecode.httl.support.parsers.CommentParser");
//		properties.put(Engine.COMPILE_DIRECTORY, this.approot+HttpRuntime.PATHSEPARATOR+"WEB-INF"+HttpRuntime.PATHSEPARATOR+"_httlcomplied");
//		properties.put(Engine.RELOADABLE, "true");*/
//		//if(this.outputenc!=null)
//		//	properties.put(Engine.OUTPUT_ENCODING,this.outputenc);
//		this.httlEngine=Engine.getEngine(properties);
		Properties httl_props=new Properties();
		httl_props.put("template.directory", this.tmproot);
		httl_props.put("template.suffix", ".httl");
		httl_props.put("reloadable", "true");
		httl_props.put("loaders","httl.spi.loaders.FileLoader");
		httl_props.put("input.encoding","UTF-8");
		httl_props.put("precompiled", "false");
		this.httlEngine=Engine.getEngine(httl_props);
	}
	public Template get(String name){
		Template template=null;
		try
		{
			if(name.endsWith("vm")){
				org.apache.velocity.Template inner= this.vtlEngine.getTemplate(name);
			    template=new VtlTemplate(inner);
			}
			else if(name.endsWith("httl")){
				httl.Template inner=this.httlEngine.getTemplate(name);
			    template=new HttlTemplate(inner);
			}
			else{
				throw new RuntimeException("Î´ÖªÊä³öÄ£°å£º"+name);
			}
		}
		catch(Exception ex){
			if(ex instanceof RuntimeException)
				throw (RuntimeException)ex;
			throw new RuntimeException(ex);
		}
		return template;
	}
	public String getPathWithExtention(String path){
		File f=new File(this.tmproot+HttpRuntime.PATHSEPARATOR+path+".vm");
		if(f.exists()){
			return path+".vm";
		}
		f=new File(this.tmproot+HttpRuntime.PATHSEPARATOR+path+".httl");
		if(f.exists()){
			return path+".httl";
		}
		return null;
		//return path+".httl";
	}
}
