package com.li.myweb;

public class HttpMethod {
	public final static String GET="GET";
	public final static String POST="POST";
	public final static String HEAD="HEAD";
	public final static String PUT="PUT";
	public final static String TRACE="TRACE";
	public final static String DELETE="DELETE";
	public final static String OPTIONS="OPTIONS";
	//获取方法对应的int值
	public static int getCode(String method){
		int c=0;
		if(method.equalsIgnoreCase(GET))
			c=1;
		else if(method.equalsIgnoreCase(POST))
			c=2;
		else if(method.equalsIgnoreCase(HEAD))
			c=4;
		else if(method.equalsIgnoreCase(PUT))
			c=8;
		else if(method.equalsIgnoreCase(TRACE))
			c=16;
		else if(method.equalsIgnoreCase(DELETE))
			c=32;
		else if(method.equalsIgnoreCase(OPTIONS))
			c=64;
		return c;
	}
}
