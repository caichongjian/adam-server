package org.caichongjian.example;

import org.caichongjian.annotations.MiniControllerScan;
import org.caichongjian.server.startup.AdamServerApplication;

/**
 * 启动类示例
 */
@MiniControllerScan(basePackages = "org.caichongjian.example.web")
public class ExampleApplication {

    public static void main(String[] args) {
        AdamServerApplication.run(ExampleApplication.class);
    }
}
