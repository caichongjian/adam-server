package org.caichongjian.server.http;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Request implements MiniHttpServletRequest {

    private RequestStream requestStream;
    private String content;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String requestURI;
    private String method;
    private Cookie[] cookies;
    private String queryString;
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);

    public Request(RequestStream requestStream) {
        this.requestStream = requestStream;
    }

    /**
     * parse the request
     */
    public void parse() throws IOException {
        content = requestStream.readRequestLineAndHeaders();
        LOGGER.debug("请求头原始内容为： {}", content);
        parseHeaders();
        LOGGER.debug("解析后的请求头为： {}", headers);
    }

    private void parseHeaders() {

        if (StringUtils.isBlank(content)) {
            LOGGER.debug("empty request");
            return;
        }

        // 解析method、uri、version、queryString
        String firstLine = content.lines().findFirst().orElseThrow();
        String[] firstLineProperties = firstLine.split(" ");
        method = firstLineProperties[0];
        requestURI = firstLineProperties[1];
        int uriSplitIndex = requestURI.indexOf('?');
        if (uriSplitIndex != -1) {
            queryString = requestURI.substring(uriSplitIndex + 1);
            requestURI = requestURI.substring(0, uriSplitIndex);
        }

        // 解析http请求的header
        content.lines().skip(1)
                .takeWhile(StringUtils::isNotBlank)
                .forEachOrdered(line -> {
                    int separationIndex = line.indexOf(": ");
                    String propertyName = line.substring(0, separationIndex);
                    String propertyValue = line.substring(separationIndex + 2);
                    headers.put(propertyName, propertyValue);
                });

        // 解析http请求header中的Cookie
        String cookieHeader = headers.get("Cookie");
        if (StringUtils.isNotBlank(cookieHeader)) {

            final List<Cookie> cookieList = Splitter.on(";").trimResults().splitToList(cookieHeader).stream()
                    .map(cookieString -> {
                        int cookieSplitIndex = cookieString.indexOf('=');
                        String name = cookieString.substring(0, cookieSplitIndex);
                        String value = cookieString.substring(cookieSplitIndex + 1);
                        return new Cookie(name, value);
                    }).collect(Collectors.toList());
            cookies = Iterables.toArray(cookieList, Cookie.class);
        }
    }

    public String readRequestBodyAsString() throws IOException {

        int contentLength = MapUtils.getIntValue(headers, "Content-Length", 0);
        if (contentLength == 0) {
            return "";
        }
        final byte[] bytes = requestStream.readRequestBody(contentLength);
        // TODO 考虑中文字符问题
        return new String(bytes);
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }
}
