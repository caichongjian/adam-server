package org.caichongjian.example;

import org.caichongjian.annotations.MiniControllerScan;
import org.caichongjian.server.startup.AdamServerApplication;

/**
 * 这是一个启动类示例，我学习参考了Spring Boot的创意。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 *
 * This is an example of a startup class, I learned to refer to the idea of Spring Boot.
 * If you think adam-server is helpful to you, please go to the link below and click ★ Star
 *
 * @see https://github.com/spring-projects/spring-boot
 */
@MiniControllerScan(basePackages = "org.caichongjian.example.web")
public class ExampleApplication {

    public static void main(String[] args) {
        AdamServerApplication.run(ExampleApplication.class);
    }
}
