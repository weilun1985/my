package com.li.myweb.annotation;

import java.lang.annotation.*;

//action�Ĳ���
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {
	String value() default "";
	int from() default 0;
}
