package com.li.myweb.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Layout {
	//是否采用模板
	@Deprecated
	boolean isNeedVm() default true; 
	//模板路径
	String template() default "";
	boolean templateOut() default true;
}