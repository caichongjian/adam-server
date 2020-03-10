package org.caichongjian.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Request {

    private InputStream inputStream;
    private String content;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String uri;
    public static final int BUFFER_SIZE = 2048;
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    public Request(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * parse the request
     */
    public void parse() throws IOException {
        parseContent();
        LOGGER.debug(content);
        parseHeaders();
        LOGGER.debug(headers.toString());
    }

    private void parseContent() throws IOException {
        // 读取客户端发送来的消息
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        // chrome浏览器(默认设置)发送一次请求后，会与服务器多次建立socket连接，且inputStream.available()经常返回0，需要在chrome设置中关闭预加载网页
        int bytes_available;
        while ((bytes_available = inputStream.available()) > 0) {

            int bytes_read = inputStream.read(buffer, 0, Math.min(bytes_available, BUFFER_SIZE));
            for (int j = 0; j < bytes_read; j++) {
                sb.append((char) buffer[j]);
            }
        }
        content = sb.toString();
    }

    private void parseHeaders() {

        if (StringUtils.isBlank(content)) {
            LOGGER.debug("empty request");
            return;
        }

        // 解析method、uri、version TODO 确定http请求中换行符是\r\n还是其他什么的
        final String[] lines = content.split("\r\n|\r|\n");
        String firstLine = lines[0];
        String[] firstLineProperties = firstLine.split(" ");
        uri = firstLineProperties[1];

        // 解析http请求的header
        for (int i = 1; i < lines.length; i++) {

            String line = lines[i];
            if (StringUtils.isBlank(line)) {
                break;
            }

            int separationIndex = line.indexOf(": ");
            String propertyName = line.substring(0, separationIndex);
            String propertyValue = line.substring(separationIndex + 2);
            headers.put(propertyName, propertyValue);
        }
    }

    public String getUri() {
        return uri;
    }
}
