package com.li.cson;

public class CSONMappedException extends CSONException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2019960878413410828L;

	public CSONMappedException(String n1,String n2){
		super(String.format("%s已添加了对%s的映射，无法再进行映射！", n1,n2));
	}
}
