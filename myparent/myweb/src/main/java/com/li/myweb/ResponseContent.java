package com.li.myweb;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

//import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

public class ResponseContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8960618436555200194L;
	private transient ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
	private Locale locale = null;
	private String contentEncoding = null;
	private String contentType = null;
	private byte[] content = null;
	//private byte[] gzipContent=null;
	private HashMap<String,String> headers=new HashMap<String,String>();
	
	public void setHeader(String key,String value){
		this.headers.put(key, value);
	}
	public void setContentType(String value){
		this.contentType=value;
	}
	public String getContentType(){
		return this.contentType;
	}
	public void setLocale(Locale locale){
		this.locale=locale;
	}
	public Locale getLocale(){
		return this.locale;
	}
	public void setContentEncoding(String value){
		this.contentEncoding=value;
	}
	public String getContentEncoding(){
		return this.contentEncoding;
	}
	public OutputStream getOutputStream(){
	   return this.bout;
	}

	public void commit(){
	    if (this.bout != null) {
	      this.content = this.bout.toByteArray();
	      try{
	    	  this.bout.close();
	      } 
	      catch (IOException e) {
			
	      }
	      finally{
	    	  this.bout=null;
	      }
	      
	    }
	}
	public void WriteTo(boolean gzip,HttpServletResponse response) throws IOException{
		if(this.contentEncoding!=null)
			response.setCharacterEncoding(this.contentEncoding);
		if(this.contentType!=null)
			response.setContentType(this.contentType);
		if(this.headers.size()>0){
			Iterator<Entry<String,String>> interator=this.headers.entrySet().iterator();
			while(interator.hasNext()){
				Entry<String,String> entry=interator.next();
				String name=entry.getKey();
				if(name.equals("Time-ms"))
					continue;
				response.setHeader(name, entry.getValue());
			}
		}
		if (this.locale != null) {
		      response.setLocale(this.locale);
		}
		OutputStream out=response.getOutputStream();
		out.write(this.content);
	}
}
