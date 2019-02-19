package com.li.myweb.caching;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;


public class OSCache {
	public OSCache(Properties props){
		//初始化全局OSCache
		_cacheAdmin=new GeneralCacheAdministrator(props);
		_cachePeriods=new ConcurrentHashMap<String,Integer>();
	}
	private GeneralCacheAdministrator _cacheAdmin;   				//缓存类
	private ConcurrentHashMap<String,Integer> _cachePeriods;        //缓存更新时间记录器

	//添加缓存
	public void set(String key,Object value,Date absoluteExpiration){
		//计算过期时间
		long timespan=(absoluteExpiration.getTime()-new Date().getTime())/1000;
		if(timespan<0)
			return;
		if(timespan>Integer.MAX_VALUE)
			timespan=-1;
		this.set(key, value, (int)timespan);
	}
	public void set(String key,Object value,int timespan){
		_cacheAdmin.putInCache(key, value);    //插入缓存
		_cachePeriods.put(key, timespan);      //插入过期时间
	}
	//读取缓存
	public Object get(String key){
		Integer periodsInt=_cachePeriods.get(key);
		int periods;
		if(periodsInt==null||(periods=periodsInt.intValue())==0)
			return null;
		try {
			return _cacheAdmin.getFromCache(key,periods);
		} catch (NeedsRefreshException e) {
			_cacheAdmin.cancelUpdate(key);
			_cachePeriods.remove(key);
			_cachePeriods.remove(key);
			return null;
		}
	}
	//删除缓存
	public void remove(String key){
		_cacheAdmin.removeEntry(key);
		_cachePeriods.remove(key);
	}
	//清除所有缓存
	public void clear(){
		_cacheAdmin.flushAll();
		_cachePeriods.clear();
	}
	public void destroy(){
		_cacheAdmin.destroy();
		_cacheAdmin=null;
		_cachePeriods=null;
	}
}
