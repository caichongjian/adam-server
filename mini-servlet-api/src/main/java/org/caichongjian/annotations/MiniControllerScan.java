package org.caichongjian.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在启动类加上此注解后，会自动扫描指定包下所有加了@MiniController注解和@MiniRestController的类，
 * 并将http请求派发给它们
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MiniControllerScan {

    /**
     * 设置需要扫描哪些包
     * @return 需要扫描的包，必填，暂不支持通配符
     */
    String[] basePackages() default {};
}
