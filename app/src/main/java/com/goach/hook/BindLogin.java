package com.goach.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author: Goach.zhong
 * Date: 2018/12/4 14:36.
 * Des:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BindLogin {
    boolean isNeedLogin() default true;
}
