package com.dili.trading.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/6/23 13:31
 * @Description: 标记防重提交
 */
@Target({METHOD})
@Retention(RUNTIME)
@Documented
public @interface ForbidDuplicateCommit {

}
