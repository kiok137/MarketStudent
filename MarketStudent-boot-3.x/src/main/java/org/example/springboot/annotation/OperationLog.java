package org.example.springboot.annotation;

import org.example.springboot.enumClass.OperationType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 操作类型
     */
    OperationType operationType() default OperationType.OTHER;
    
    /**
     * 操作模块
     */
    String operationModule() default "";
    
    /**
     * 操作描述
     */
    String operationDesc() default "";
} 