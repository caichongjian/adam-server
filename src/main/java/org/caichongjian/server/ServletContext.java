package org.caichongjian.server;

public class ServletContext {

    private static class ServletContextHolder {
        private static final ServletContext INSTANCE = new ServletContext();
    }

    /**
     * 根目录
     */
    private String rootPath;

    /**
     * static目录，用于存放web相关的静态文件
     */
    private String webRootPath;


    private ServletContext() {
        rootPath = getClass().getResource("/").getFile();
        webRootPath = rootPath + "static/";
    }

    public static ServletContext getInstance() {
        return ServletContextHolder.INSTANCE;
    }

    public String getWebRootPath() {
        return webRootPath;
    }
}
