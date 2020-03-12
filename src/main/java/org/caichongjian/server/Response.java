package org.caichongjian.server;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

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

    public static final String NOT_FOUND_TEMPLATE = "HTTP/1.1 404 File Not Found\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: 23\r\n" +
            "\r\n" +
            "<h1>File Not Found</h1>";

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 发送静态资源到浏览器
     *
     * @param uri 静态资源的相对路径
     * @throws IOException IO异常
     */
    public void sendStaticResource(String uri) throws IOException {

        uri = (StringUtils.isBlank(uri) || "/".equals(uri)) ? "index.html" : uri;

        final Path path = Paths.get(ServletContext.getInstance().getWebRootPath(), uri);
        if (Files.exists(path)) {
            final byte[] bytes = Files.readAllBytes(path);
            String responseHeader = Strings.lenientFormat(OK_HEADER_TEMPLATE, bytes.length);
            outputStream.write(responseHeader.getBytes());
            outputStream.write(bytes);
        } else {
            outputStream.write(NOT_FOUND_TEMPLATE.getBytes());
        }
    }
}
