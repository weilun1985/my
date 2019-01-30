package com.li.myweb;

import javax.servlet.http.Cookie;

public class HttpCookie extends Cookie {
	/**
	 * 
	 */
	private static final long serialVersionUID = 219473843067318040L;
	
	public HttpCookie(String name){
		super(name, null);
	}
	public HttpCookie(String name,String value){
		super(name,value);
	}
}
