package com.li.myweb.templates;

import java.io.Writer;
import java.util.Map;

import com.li.myweb.HttpContext;

public class TemplateBase implements Template {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void render(Map<String, Object> map, Writer writer) {
        // add envionment|global variables
        Map map0=HttpContext.current().server().getConfigure().getVariables();
        map.putAll(map0);
    }

}

