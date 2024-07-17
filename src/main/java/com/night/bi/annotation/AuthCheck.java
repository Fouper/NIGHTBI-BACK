package com.night.bi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 *
 * @author WL丶Night
 * @from WL丶Night
 */

// 目标：即在哪里使用（方法METHOD，类TYPE）
@Target(ElementType.METHOD)
// 存活范围（生命周期）：（仅存在源代码中SOURCE，编译时可通过反射获取注解的信息CLASS，用于运行时需要处理RUNTIME）
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";

}

