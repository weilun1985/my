package com.li.cson;

public class CSONException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8519227622096153833L;

	public CSONException(String string){
		// TODO Auto-generated constructor stub
		super(string);
	}
	public CSONException() {
		super();
	}
    public CSONException(String message,Exception inner){
    	super(message,inner);
    }
}
