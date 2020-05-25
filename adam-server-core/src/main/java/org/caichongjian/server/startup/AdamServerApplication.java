package org.caichongjian.server.startup;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.caichongjian.annotations.MiniControllerScan;
import org.caichongjian.annotations.MiniRestController;
import org.caichongjian.server.RestProcessor;
import org.caichongjian.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责校验配置，并扫描Controller
 * <p/>
 * <p>截止到2020年下午2点28分为止，本Git仓库 0 fork， 0 Star；</p>
 * <p>GitHub 的 Insights 显示 3 Clones，3 Unique cloners，约等于我自己 Clone 的次数；</p>
 * <p>GitHub 的 Insights 显示 73 Views，6 Unique visitors，约等于我自己查看的次数</p>
 */
public class AdamServerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdamServerApplication.class);

    private AdamServerApplication() {
    }

    public static void run(Class<?> primarySource) {

        LOGGER.info("开始启动服务器....");

        LOGGER.info("开始扫描RestController注解....");

        // 根据启动类上注解提供的信息，扫描Controller
        final MiniControllerScan annotation = primarySource.getAnnotation(MiniControllerScan.class);

        if (annotation == null) {
            LOGGER.error("请在启动类加上@MiniControllerScan注解");
            System.exit(1);
        }

        // 解决引入Jigsaw后在IDE中启动，主页404的问题。实现方式我不太满意，有机会再考虑优化
        ServerContext.getInstance().setPrimarySource(primarySource);

        // 扫描RestController
        final String[] basePackages = annotation.basePackages();
        for (String basePackage : basePackages) {
            try {
                // TODO ClassPath类上有@Beta注解，它标识此API是not "API-frozen"的，即未来版本中它可能修改或删除。也就是提醒使用者使用它要慎重。这个需要慎重使用的类目前貌似暂时和Jigsaw不太兼容
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
