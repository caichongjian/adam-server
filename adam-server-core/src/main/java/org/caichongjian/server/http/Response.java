package org.caichongjian.server.http;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.server.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Response {

    private OutputStream outputStream;
    public static final String OK_HEADER_TEMPLATE = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: %s\r\n" +
            "\r\n";

    public static final String DEFAULT_INDEX_TEMPLATE = OK_HEADER_TEMPLATE + "<h1>Hello World!!!</h1>";

    public static final String JSON_OK_HEADER_TEMPLATE = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Length: %s\r\n" +
            "\r\n";

    public static final String NOT_FOUND_TEMPLATE = "HTTP/1.1 404 File Not Found\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: 23\r\n" +
            "\r\n" +
            "<h1>File Not Found</h1>";

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 发送json到浏览器
     *
     * @param jsonString json字符串
     * @throws IOException IO异常
     */
    public void sendJsonString(String jsonString) throws IOException {
        // TODO 考虑中文字符
        final byte[] bytes = jsonString.getBytes();
        String responseHeader = Strings.lenientFormat(JSON_OK_HEADER_TEMPLATE, bytes.length);
        outputStream.write(responseHeader.getBytes());
        outputStream.write(bytes);
    }

    /**
     * 发送静态资源到浏览器
     *
     * @param uri 静态资源的相对路径
     * @throws IOException IO异常
     */
    public void sendStaticResource(String uri) throws IOException {

        uri = (StringUtils.isBlank(uri) || "/".equals(uri)) ? "index.html" : uri;

        final Path path = Paths.get(ApplicationContext.getInstance().getWebRootPath(), uri);
        if (Files.exists(path)) {
            final byte[] bytes = Files.readAllBytes(path);
            String responseHeader = Strings.lenientFormat(OK_HEADER_TEMPLATE, bytes.length);
            outputStream.write(responseHeader.getBytes());
            outputStream.write(bytes);
        } else {
            String template = "index.html".equals(uri) ? DEFAULT_INDEX_TEMPLATE : NOT_FOUND_TEMPLATE;
            outputStream.write(template.getBytes());
        }
    }
}
