package org.caichongjian.server;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Response {

    private OutputStream outputStream;
    public static final String BASE_MESSAGE = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: %s\r\n" +
            "\r\n";

    public static final String ERROR_MESSAGE = "HTTP/1.1 404 File Not Found\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: 23\r\n" +
            "\r\n" +
            "<h1>File Not Found</h1>";

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 发送静态资源到浏览器
     * @param uri 静态资源的相对路径
     * @throws IOException IO异常
     */
    public void sendStaticResource(String uri) throws IOException {
        final Path path = Paths.get(ServletContext.getInstance().getWebRootPath(), uri);
        if (Files.exists(path)) {
            final byte[] bytes = Files.readAllBytes(path);
            String responseHeader = Strings.lenientFormat(BASE_MESSAGE, bytes.length);
            outputStream.write(responseHeader.getBytes());
            outputStream.write(bytes);
        } else {
            outputStream.write(ERROR_MESSAGE.getBytes());
        }
    }
}
