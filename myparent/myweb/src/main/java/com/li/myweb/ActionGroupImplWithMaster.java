package com.li.myweb;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/*import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;*/

import com.li.myweb.exceptions.NeedSetMasterInfoException;
import com.li.myweb.templates.Template;
@Deprecated
public abstract class ActionGroupImplWithMaster extends ActionGroupImpl {
	//protected Context masterTemplateContext;
	protected Map<String,Object> masterTemplateContext;
	private String masterTemplate;
	private String contentTag="_CONTENT";
	@Override
	public void init(){
		super.init();
		this.masterTemplateContext=new HashMap<String,Object>();
	}
	protected void addToMaster(String key,Object obj){
		this.masterTemplateContext.put(key, obj);
	}
	protected void setMaster(String masterTemplateName,String contentTagName){
		this.masterTemplate=masterTemplateName;
		this.contentTag=contentTagName;
	}
	protected void setMaster(String masterTemplateName){
		this.masterTemplate=masterTemplateName;
	}
	protected void renameContentTag(String name){
		this.contentTag=name;
	}
	@Override
	public void mergeOut(Template template,Writer writer){
		if(this.masterTemplate==null||this.masterTemplate.length()==0||this.contentTag==null||this.contentTag.length()==0)
			throw new NeedSetMasterInfoException(this);
		java.io.StringWriter cwriter=new java.io.StringWriter();
		super.mergeOut(template, cwriter);
		String content=cwriter.getBuffer().toString();
		this.masterTemplateContext.put(contentTag, content);
		Template temp=this.server.getTemplate(this.masterTemplate);
		//temp.merge(this.masterTemplateContext, writer);
		temp.render(this.masterTemplateContext, writer);
	}
}
