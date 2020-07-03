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
        /**
         * <p>adam-server的定位是学习材料，学习材料本身没必要实现这么多特性。</p>
         * <p>小伙伴们如果感兴趣的话，可以将它作为一道练习题，自己尝试着实现文件的上传下载特性。</p>
         */
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    }

    /**
     * 也可以将这些配置放到配置文件里
     */
    public static final class Server {

        private Server() {
        }

        public static final int PORT = 8888; // 端口号，可根据实际情况调整
        public static final int THREAD_POOL_SIZE = 20; // 线程池大小，可根据实际情况调整
        public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8"); // 默认字符集，可根据实际情况调整

        /**
         * 连接超时时间，单位为毫秒。
         * 经初步测试发现socketInputStream.read()阻塞时间超过这个值的请求将被打断。
         * 如果设置为null、负数或者0，服务器端将不限制socketInputStream.read()的超时时间。
         */
        public static final Integer CONNECTION_TIMEOUT_MILLIS = null;

    }

}
