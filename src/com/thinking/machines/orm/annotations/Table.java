package com.thinking.machines.orm.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table
{
public String name();
}