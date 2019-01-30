package com.li.myweb.supports;

import com.li.myweb.IEtagSupported;
import com.li.myweb.ILastModifySupported;
import com.li.myweb.Utils;
import com.li.myweb.annotation.Master;
import com.li.myweb.annotation.OutputCache;
import com.li.myweb.annotation.Layout;

public class ActionGroupInfo {
	public ActionGroupInfo(Class<?> inst){
		this.impl=inst;
		Layout layout=inst.getAnnotation(Layout.class);
		this.layout=layout;
		
		OutputCache cctrl=inst.getAnnotation(OutputCache.class);
		this.outputCache=cctrl;
		
		Master master=inst.getAnnotation(Master.class);
		this.master=master;
		boolean[] impls=Utils.isImplements(inst, new Class<?>[]{IEtagSupported.class,ILastModifySupported.class});
		etagEnable=impls[0];
		modifiedEnable=impls[1];
	}
	public final Layout layout;               //布局相关
	public final OutputCache outputCache;     //输出缓存控制
	public final Master master;               //母版
	public final Class<?> impl;         	  //类型
	public final boolean etagEnable;          //是否实现IETag接口
	public final boolean modifiedEnable;      //是否实现ILastModified接口
	@Override
	public String toString(){
		return this.impl.toString()
		+" [EtagEnable="+etagEnable+";"+"ModifiedEnable="+modifiedEnable+"]"
		+(this.layout==null?"":(" [Layout:IsNeedVm="+this.layout.isNeedVm()+" Template="+this.layout.template()+"]"));
	}
}
