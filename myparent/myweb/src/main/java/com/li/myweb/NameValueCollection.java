package com.li.myweb;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NameValueCollection extends LinkedHashMap<String, String[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NameValueCollection(){
		
	}
	@Override
	public String[] get(Object obj){
		String key=obj.toString().toLowerCase();
		return super.get(key);
	}
	@Override
	public String[] put(String key,String[] value){
		return super.put(key.toLowerCase(), value);
	}
}
