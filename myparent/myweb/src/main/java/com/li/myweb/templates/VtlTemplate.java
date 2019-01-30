package com.li.myweb.templates;

import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

public class VtlTemplate extends TemplateBase {
	public VtlTemplate(org.apache.velocity.Template temp){
		this.mTemplate=temp;
	}
	private org.apache.velocity.Template mTemplate;
	//@Override
	public void render(Map<String,Object> map, Writer writer) {
		// TODO Auto-generated method stub
		super.render(map, writer);
		Context context=new VelocityContext();
		for(Entry<String, Object> entry:map.entrySet()){
			context.put(entry.getKey(), entry.getValue());
		}
		this.mTemplate.merge(context, writer);
	}

}
