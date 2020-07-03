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
 * <p>截止到2020年6月11日晚上10点00分为止，本Git仓库在两个平台上加起来 0 fork， 0 Star；</p>
 * <p>公开的两个电子邮箱从2020年3月初到6月6日收到 0 封非系统发送的邮件(我自己测试邮箱是否有效的邮件除外；“捕鱼送28元，+V;17320124513领体验最
 * 好玩的”、“旺年宋旺旺 新入住忪22沅，豁洞立刻聆添加美女企鹅【839283634】高配9.95”这样的两封奇奇怪怪的邮件除外；邮箱空间未满；邮箱的垃圾箱也查
 * 看过；邮箱账号也没有被盗)</p>
 * <p>GitHub 上本Git仓库的 Insights 显示 5月29日到6月11日期间 1 Clones，1 Unique cloners，约等于我自己 Clone 的次数；</p>
 * <p>GitHub 上本Git仓库的 Insights 显示 5月29日到6月11日期间 17 Views，4 Unique visitors，约等于我自己查看的次数</p>
 * <p>gitee.com 上本Git仓库的截止到6月11日的访问统计显示 全部 IP = 8，全部 PULL = 1，约等于我自己操作的次数</p>
 * <p/>
 * <p>这数据让我感到困惑，因为我在某招聘网站上公开简历已经29天了，期间有190+个人(既有HR、也有技术、猎头和其他)对我发起沟通。
 * 我简历上四次工作经历写了四个“忘得差不多了”；自我评价写了81个字，其中重点描述了自己的情况；另外除了个人基本信息之外就放了个GitHub链接。
 * 我不知道哪个地方出了什么问题，但总感觉GitHub上显示的查看数和招聘网站上显示的沟通数不成正比看起来很奇怪。</p>
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
