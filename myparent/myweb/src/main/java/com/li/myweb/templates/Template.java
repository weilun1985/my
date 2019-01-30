package com.li.myweb.templates;

import java.io.Writer;
import java.util.Map;

public interface Template {
	public void render(Map<String,Object> map,Writer writer);
}
