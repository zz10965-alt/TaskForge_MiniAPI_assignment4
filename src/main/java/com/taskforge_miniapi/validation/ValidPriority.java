package com.taskforge_miniapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD,ElementType.PARAMETER})//定义这个注解在哪个对象生效（字段与参数）
@Retention(RetentionPolicy.RUNTIME)//注解在什么环境运行时有效
@Documented //该注解会被javadoc记录
@Constraint(validatedBy=PriorityValidator.class)//表示该注解作为validation
public @interface ValidPriority {
    String message()default"Priority must be LOW/MEDIUM/HIGH";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
