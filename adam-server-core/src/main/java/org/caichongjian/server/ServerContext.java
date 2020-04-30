package org.caichongjian.server;

import java.io.IOException;
import java.io.InputStream;

public class ServerContext {

    private static class ServerContextHolder {
        private static final ServerContext INSTANCE = new ServerContext();
    }

    private Class<?> primarySource;

    private ServerContext() {
    }

    /**
     * 设置启动类的Class
     * 解决引入Jigsaw后在IDE中启动，主页404的问题
     * 内部方法，仅供adam-server-core内部调用
     *
     * @param primarySource 启动类的Class
     */
    public void setPrimarySource(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    public static ServerContext getInstance() {
        return ServerContextHolder.INSTANCE;
    }

    public byte[] getStaticResource(String uri) throws IOException {
        InputStream inputStream = primarySource.getResourceAsStream("/static" + uri);
        return inputStream == null ? null : inputStream.readAllBytes();
    }
}
