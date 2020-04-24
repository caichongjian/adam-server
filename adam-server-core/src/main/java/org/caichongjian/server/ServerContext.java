package org.caichongjian.server;

import java.io.IOException;
import java.io.InputStream;

public class ServerContext {

    private static class ServerContextHolder {
        private static final ServerContext INSTANCE = new ServerContext();
    }

    private ServerContext() {
    }

    public static ServerContext getInstance() {
        return ServerContextHolder.INSTANCE;
    }

    public byte[] getStaticResource(String uri) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/static/" + uri);
        return inputStream == null ? null : inputStream.readAllBytes();
    }
}
