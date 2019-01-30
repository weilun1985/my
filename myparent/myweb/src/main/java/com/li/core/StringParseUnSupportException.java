package com.li.core;

public class StringParseUnSupportException extends Exception {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	public StringParseUnSupportException(Class c,String s,Throwable ex){
		super(String.format("无法将“%s”转换为类型%s的值,异常=%s",s,c,ex.toString()));
	}
	@SuppressWarnings("rawtypes")
	public StringParseUnSupportException(Class c,String s){
		super(String.format("无法将“%s”转换为类型%s的值",s,c));
	}
}
