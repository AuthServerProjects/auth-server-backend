package com.behpardakht.oauth_server.authorization.aspect;

import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    AuditAction action();
    String username() default "";
    String clientId() default "";
    String details() default "";
}