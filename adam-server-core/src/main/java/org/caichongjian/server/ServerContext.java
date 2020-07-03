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
        // TODO 存在客户端能通过HTTP请求拿到class文件的安全隐患(经初步测试发现浏览器不允许URL中出现.. 但直接通过socket发送的请求只能在服务端进行限制)
        InputStream inputStream = primarySource.getResourceAsStream("/static" + uri);
        return inputStream == null ? null : inputStream.readAllBytes();
    }
}
