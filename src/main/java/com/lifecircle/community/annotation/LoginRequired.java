package com.lifecircle.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用这个注解标记某个方法，结合拦截器来判断是否登录，才能访问该方法
 */
// 注解作用域
@Target(ElementType.METHOD)
// 注解作用时间
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
