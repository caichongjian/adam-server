package org.caichongjian.server;

import java.io.IOException;
import java.io.InputStream;

public class ApplicationContext {

    private static class ApplicationContextHolder {
        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }

    private ApplicationContext() {
    }

    public static ApplicationContext getInstance() {
        return ApplicationContextHolder.INSTANCE;
    }

    public byte[] getStaticResource(String uri) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/static/" + uri);
        return inputStream == null ? null : inputStream.readAllBytes();
    }
}
