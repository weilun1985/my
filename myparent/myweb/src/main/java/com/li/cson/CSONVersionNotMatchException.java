package com.li.cson;

public class CSONVersionNotMatchException extends CSONException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6022574737316758374L;
	public CSONVersionNotMatchException(byte proto) {
		super(String.format("无法解析CSON数据：CSON数据的版本与当前版本不一致,数据版本：%d，当前版本：%d", proto, CSON2.PROTOVERSION));
	}
}
