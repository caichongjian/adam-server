package org.caichongjian.server;

public class ApplicationContext {

    private static class ApplicationContextHolder {
        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }

    /**
     * 根目录
     */
    private String rootPath;

    /**
     * static目录，用于存放web相关的静态文件
     */
    private String webRootPath;


    private ApplicationContext() {
        rootPath = getClass().getResource("/").getFile();
        webRootPath = rootPath + "static/";
    }

    public static ApplicationContext getInstance() {
        return ApplicationContextHolder.INSTANCE;
    }

    public String getWebRootPath() {
        return webRootPath;
    }
}
