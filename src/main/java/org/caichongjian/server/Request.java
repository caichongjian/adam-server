package org.caichongjian.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Request implements MiniHttpServletRequest {

    private InputStream inputStream;
    private String content;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String uri;
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    public Request(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * parse the request
     */
    public void parse() throws IOException {
        parseContent();
        LOGGER.debug("请求体内容为： {}", content);
        parseHeaders();
        LOGGER.debug("解析后的请求头为： {}", headers);
    }

    private void parseContent() throws IOException {
        // 读取浏览器发送来的消息
        byte[] bytes = new byte[inputStream.available()];
        IOUtils.readFully(inputStream, bytes);

        // chrome浏览器(默认设置)发送一次请求后，会与服务器多次建立socket连接，且inputStream.available()经常返回0，需要在chrome设置中关闭预加载网页
        content = new String(bytes);
    }

    private void parseHeaders() {

        if (StringUtils.isBlank(content)) {
            LOGGER.debug("empty request");
            return;
        }

        // 解析method、uri、version
        String firstLine = content.lines().findFirst().orElseThrow();
        String[] firstLineProperties = firstLine.split(" ");
        uri = firstLineProperties[1];

        // 解析http请求的header
        content.lines().skip(1)
                .takeWhile(StringUtils::isNotBlank)
                .forEachOrdered(line -> {
                    int separationIndex = line.indexOf(": ");
                    String propertyName = line.substring(0, separationIndex);
                    String propertyValue = line.substring(separationIndex + 2);
                    headers.put(propertyName, propertyValue);
                });
    }

    public String getUri() {
        return uri;
    }
}
