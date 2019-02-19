package com.li.myweb.exceptions;

import com.li.myweb.ActionGroupImpl;

public class NeedSetMasterInfoException extends RuntimeException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -4627619990152986777L;

	public NeedSetMasterInfoException(ActionGroupImpl ag){
		super(String.format("需要为%s指定母版页信息。请通过setMaster方法进行设置！",ag.getClass().getName()));
	}
}
