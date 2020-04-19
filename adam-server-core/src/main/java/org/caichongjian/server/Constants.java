package org.caichongjian.server;

import java.nio.charset.Charset;

/**
 * 常量
 */
public final class Constants {

    private Constants() {
    }

    public static final class ContentType {

        private ContentType() {
        }

        public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String APPLICATION_JSON = "application/json";
        public static final String TEXT_HTML = "text/html";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    }

    public static final class Server {

        private Server() {
        }

        public static final int PORT = 8888;
        public static final int THREAD_POOL_SIZE = 20;
        public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    }

}
