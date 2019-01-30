package com.li.myweb.exceptions;

public class TemplateException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8740932099505150631L;

	public TemplateException(){
		
	}
	public TemplateException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TemplateException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public TemplateException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
