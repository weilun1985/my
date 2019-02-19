package com.li.myweb.templates;

import java.io.Writer;
import java.util.Map;

public class HttlTemplate extends TemplateBase {
	public HttlTemplate(httl.Template temp){
		this.mtemplate=temp;
	}
	private httl.Template mtemplate;
	@Override
	public void render(Map<String, Object> map, Writer writer){
		// TODO Auto-generated method stub
		super.render(map, writer);
		try
		{
			this.mtemplate.render(map, writer);
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

}
