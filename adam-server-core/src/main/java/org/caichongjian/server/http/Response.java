package org.caichongjian.server.http;

import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.server.ApplicationContext;
import org.caichongjian.server.Constants;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class Response implements MiniHttpServletResponse {

    private OutputStream outputStream;
    private Map<String, String> headers = new LinkedHashMap<>();
    private List<Cookie> cookies = new LinkedList<>();
    private ResponseLine responseLine = ResponseLine.OK;

    private enum ResponseLine {
        OK("HTTP/1.1 200 OK"),
        NOT_FOUND("HTTP/1.1 404 File Not Found");
        private String text;

        ResponseLine(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public static final String NOT_FOUND_TEMPLATE = "<h1>Page Not Found</h1>";

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
        setContentType(Constants.ContentType.APPLICATION_JSON);
        setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length));

        String responseLineAndHeader = responseLineAndHeadersToString();
        outputStream.write(responseLineAndHeader.getBytes());
        outputStream.write(bytes);
    }

    /**
     * 发送静态资源到浏览器
     *
     * @param uri 静态资源的相对路径
     * @throws IOException IO异常
     */
    public void sendStaticResource(String uri) throws IOException{

        uri = (StringUtils.isBlank(uri) || "/".equals(uri)) ? "index.html" : uri;

        byte[] bytes = ApplicationContext.getInstance().getStaticResource(uri);
        if (ArrayUtils.isEmpty(bytes)) {
            bytes = NOT_FOUND_TEMPLATE.getBytes();
            responseLine = ResponseLine.NOT_FOUND;
        }
        setContentType(Constants.ContentType.TEXT_HTML);
        setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length));
        String responseLineAndHeader = responseLineAndHeadersToString();
        outputStream.write(responseLineAndHeader.getBytes());
        outputStream.write(bytes);
    }

    /**
     * 将响应行和响应头从内部格式转成字符串
     *
     * @return 字符串格式的响应行和响应头
     */
    private String responseLineAndHeadersToString() {

        StringBuilder sb = new StringBuilder(128);
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
