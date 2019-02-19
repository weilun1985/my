package com.li.myweb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import com.li.myweb.HttpRuntime;

//控制页面缓存
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface OutputCache {
	//是否启用页面Cache
	boolean enable() default true;   
	//缓存依赖，参数串，Cookie,默认采用URI+ACTION
	int dependTypes() default 0;   
	//缓存时间,默认5分钟
	int periods() default HttpRuntime.OUTPUTCACHEPERIODS;
	//是否在浏览器端启用Cache-Control
	boolean enableCacheControl() default false;
}
