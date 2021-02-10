package com.blamejared.crafttweaker_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EventCancelable {
    
    /**
     * if the event is cancelable
     */
    boolean value() default true;
    
    /**
     * what happens if the event is canceled?
     */
    String canceledDescription() default "";
    
    /**
     * what happens if the event is not canceled?
     */
    String notCanceledDescription() default "";
}