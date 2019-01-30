package com.li.myweb.exceptions;

public class UnAllowedHttpMethodException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2516159668702598686L;
	public UnAllowedHttpMethodException() {
		// TODO Auto-generated constructor stub
	}

	public UnAllowedHttpMethodException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnAllowedHttpMethodException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnAllowedHttpMethodException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
