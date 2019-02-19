package com.li.myweb.caching;

import com.li.myweb.ResponseContent;
import com.opensymphony.oscache.web.filter.SplitServletOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

public class CacheHttpServletResponseWrapper extends HttpServletResponseWrapper {
	public CacheHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		// TODO Auto-generated constructor stub
		respContent=new ResponseContent();
	}
	private ResponseContent respContent;
	private PrintWriter cachedWriter = null;
	private SplitServletOutputStream cacheOut = null;
	
	
	public ResponseContent getContent(){
		respContent.commit();
		return respContent;
	}
	@Override
	public void addHeader(String name,String value){
		super.addHeader(name, value);
		respContent.setHeader(name, value);
	}
	@Override
	public void setHeader(String name,String value){
		super.setHeader(name, value);
		respContent.setHeader(name, value);
	}
	@Override
	public void setLocale(Locale loc){
		super.setLocale(loc);
		respContent.setLocale(loc);
	}
	@Override
	public void setContentType(String type){
		super.setContentType(type);
		respContent.setContentType(type);
	}
	@Override
	public void setCharacterEncoding(String charset){
		super.setCharacterEncoding(charset);
		respContent.setContentEncoding(charset);
	}
	@Override
	public ServletOutputStream getOutputStream()
	    throws IOException
	{
	    if (this.cacheOut == null) {
	      this.cacheOut = new SplitServletOutputStream(this.respContent.getOutputStream(), super.getOutputStream());
	    }
	
	    return this.cacheOut;
	}
	@Override
	public PrintWriter getWriter()
    throws IOException
    {
	    if (this.cachedWriter == null) {
	      String encoding = getCharacterEncoding();
	      if (encoding != null)
	        this.cachedWriter = new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding));
	      else {
	        this.cachedWriter = new PrintWriter(new OutputStreamWriter(getOutputStream()));
	      }
	    }
	    return this.cachedWriter;
    }
	private void flush()
    throws IOException
    {
	    if (this.cacheOut != null) {
	      this.cacheOut.flush();
	    }
	
	    if (this.cachedWriter != null)
	      this.cachedWriter.flush();
    }
      @Override
	  public void flushBuffer() throws IOException
	  {
	    super.flushBuffer();
	    flush();
	  }
}
