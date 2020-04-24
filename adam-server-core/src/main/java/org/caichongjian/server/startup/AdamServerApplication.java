package org.caichongjian.server.startup;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.caichongjian.annotations.MiniControllerScan;
import org.caichongjian.annotations.MiniRestController;
import org.caichongjian.server.RestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责校验配置，并扫描Controller
 */
public class AdamServerApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(AdamServerApplication.class);

    public static void run(Class<?> primarySource) {

        LOGGER.info("开始启动服务器....");

        LOGGER.info("开始扫描RestController注解....");

        // 根据启动类上注解提供的信息，扫描Controller
        final MiniControllerScan annotation = primarySource.getAnnotation(MiniControllerScan.class);

        if (annotation == null) {
            LOGGER.error("请在启动类加上@MiniControllerScan注解");
            System.exit(1);
        }

        // 扫描RestController
        final String[] basePackages = annotation.basePackages();
        for (String basePackage : basePackages) {
            try {
                final ImmutableSet<ClassPath.ClassInfo> classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
                for (ClassPath.ClassInfo classInfo : classInfos) {
                    final Class<?> clazz = classInfo.load();
                    final MiniRestController miniRestController = clazz.getAnnotation(MiniRestController.class);
                    if (miniRestController != null) {
                        RestProcessor.addUriMapping(clazz);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                LOGGER.error("扫描MiniController失败", e);
                System.exit(1);
            }
        }

        HttpServer.start();
    }
}
