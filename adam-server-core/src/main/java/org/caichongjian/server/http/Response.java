package org.caichongjian.server.http;

import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.server.ServerContext;
import org.caichongjian.server.Constants;

import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class Response implements MiniHttpServletResponse {

    private final OutputStream outputStream;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<Cookie> cookies = new LinkedList<>();
    private ResponseLine responseLine = ResponseLine.OK;

    private enum ResponseLine {
        // 可以根据自己的喜好增加500、302等等
        OK("HTTP/1.1 200 OK"), // 这里的HTTP/1.1可以改成HTTP/1.0或者HTTP/2.0或者其他版本
        NOT_FOUND("HTTP/1.1 404 NOT FOUND");
        private final String text;

        ResponseLine(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static final String NOT_FOUND_TEMPLATE = "<h1>Not found.</h1>"; // 可以根据自己的喜好修改相关代码，定制404页面

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 发送JSON到浏览器
     * <p>【也可以根据自己的喜好选择移除此方法，改为统一使用sendDynamicResource方法】</p>
     * <p>【也可以根据自己的喜好选择移除此方法，改为新建一个数据成员中包含Response的类或者
     * 新建一个单纯的工具类，然后将相关的发送JSON资源、发送XML资源等方法放到那个新建的类中】</p>
     *
     * @param jsonString JSON字符串
     * @throws IOException IO异常
     */
    public void sendJsonDynamicResource(String jsonString) throws IOException {
        sendDynamicResource(stringToBytes(jsonString), Constants.ContentType.APPLICATION_JSON);
    }

    /**
     * 发送动态资源(如动态生成的JSON、动态生成的HTML等等)到浏览器
     *
     * @param responseBody 动态资源(如动态生成的JSON、动态生成的HTML等等)
     * @param contentType  内容类型，如"application/json"、"text/html"等等。
     * @throws IOException IO异常
     */
    public void sendDynamicResource(byte[] responseBody, String contentType) throws IOException {

        setContentType(contentType);
        setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseBody.length));

        String responseLineAndHeader = responseLineAndHeadersToString();
        outputStream.write(stringToBytes(responseLineAndHeader)); // 响应头指定UTF-8是否有必要?
        outputStream.write(responseBody);
    }

    /**
     * 发送静态资源到浏览器
     *
     * @param uri 静态资源的相对路径
     * @throws IOException IO异常
     */
    public void sendStaticResource(String uri) throws IOException {

        uri = (StringUtils.isBlank(uri) || "/".equals(uri)) ? "/index.html" : uri;

        byte[] responseBody = ServerContext.getInstance().getStaticResource(uri);
        if (ArrayUtils.isEmpty(responseBody)) {
            responseBody = stringToBytes(NOT_FOUND_TEMPLATE);
            responseLine = ResponseLine.NOT_FOUND;
        }
        // TODO 感兴趣的朋友可以改造代码，使其支持js、css、图片等静态资源
        String contentType = Constants.ContentType.TEXT_HTML;
        sendDynamicResource(responseBody, contentType); // 也可以将sendDynamicResource方法中的代码复制到这里
    }

    /**
     * 将响应行和响应头从内部格式转成字符串
     *
     * @return 字符串格式的响应行和响应头
     */
    private String responseLineAndHeadersToString() {

        StringBuilder sb = new StringBuilder(128);  // 可根据实际情况调整
        sb.append(responseLine.getText()).append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
            if (cookie.getMaxAge() != -1) {
                sb.append("Max-Age=").append(cookie.getMaxAge()).append("; "); // 不考虑兼容Expires了
            }
            Optional.ofNullable(cookie.getDomain()).ifPresent(domain -> sb.append("Domain=").append(domain).append("; "));
            Optional.ofNullable(cookie.getPath()).ifPresent(path -> sb.append("Path=").append(path).append("; "));
            if (cookie.getSecure()) {
                sb.append("Secure; ");
            }
            if (cookie.isHttpOnly()) {
                sb.append("HttpOnly; ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    private byte[] stringToBytes(String str) {
        // 可以根据自己的喜好选择字符集
        return str.getBytes(Constants.Server.DEFAULT_CHARSET);
    }

    @Override
    public void addCookie(Cookie cookie) {
        Preconditions.checkNotNull(cookie, "参数[cookie]不能为null");
        // cookie中的特殊字符需不需要server来特别处理？
        cookies.add(cookie);
    }

    @Override
    public void setHeader(String name, String value) {
        Preconditions.checkNotNull(name, "参数[name]不能为null");
        Preconditions.checkNotNull(value, "参数[value]不能为null");
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        Preconditions.checkNotNull(name, "参数[name]不能为null");
        return headers.get(name);
    }

    @Override
    public String getContentType() {
        return getHeader(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public void setContentType(String type) {
        Preconditions.checkNotNull(type, "参数[type]不能为null");
        setHeader(HttpHeaders.CONTENT_TYPE, type);
    }
}
