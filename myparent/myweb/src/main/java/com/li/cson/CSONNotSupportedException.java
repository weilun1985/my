package com.li.cson;

public class CSONNotSupportedException extends CSONException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4056870175196917000L;
	@SuppressWarnings("rawtypes")
	public CSONNotSupportedException(Class cls){
		super(String.format("CSON数据类型未提供对“%s”类型的转换支持！",cls.getName()));
	}
}
