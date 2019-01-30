package com.li.myweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpUploadedFileCollection {
	private List<HttpUploadedFile> list=new ArrayList<HttpUploadedFile>();
	private Map<String,HttpUploadedFile> hm=new LinkedHashMap<String,HttpUploadedFile>();
	public void add(HttpUploadedFile file){
		this.list.add(file);
		String key=file.getFieldName();
		if(key!=null&&key.length()>0){
			if(this.hm.containsKey(key))
				key+="/"+this.list.size();
		}
		else{
			key="/"+this.list.size();
		}
		this.hm.put(key, file);
	}
	public HttpUploadedFile get(String field){
		return this.hm.get(field);
	}
	public HttpUploadedFile get(int index){
		return this.list.get(index);
	}
	public int count(){
		return this.list.size();
	}
}
