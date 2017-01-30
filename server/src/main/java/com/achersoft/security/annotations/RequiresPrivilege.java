package com.achersoft.security.annotations;

import com.achersoft.security.type.Privilege;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
public @interface RequiresPrivilege {
    public Privilege[] value();
}
