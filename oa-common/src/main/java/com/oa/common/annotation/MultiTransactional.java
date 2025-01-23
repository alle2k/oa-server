package com.oa.common.annotation;

import com.oa.common.constant.TransactionConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiTransactional {

    String[] values() default {TransactionConstant.MASTER, TransactionConstant.FLOWABLE};
}
