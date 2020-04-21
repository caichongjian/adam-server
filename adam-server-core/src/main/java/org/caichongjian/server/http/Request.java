package org.caichongjian.server.http;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.net.HttpHeaders;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.server.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Request implements MiniHttpServletRequest {

    private RequestStream requestStream;
    private Map<String, String> headers = new LinkedHashMap<>();
    private ListMultimap<String, String> parameters = ArrayListMultimap.create();
    private String requestURI;
    private String method;
    private Cookie[] cookies;
    private String queryString;
    private String requestBodyString; // 解析完请求体以后不会再使用它了。但还是先留着吧，万一以后出了什么bug也好调试
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    private static final Splitter AMP_SPLITTER = Splitter.on("&").omitEmptyStrings();

    public Request(RequestStream requestStream) {
        this.requestStream = requestStream;
    }

    /**
     * parse the request
     */
    public void parseRequestLineAndHeaders() throws IOException {
        String requestLineAndHeadersString = requestStream.readRequestLineAndHeaders();

        if (StringUtils.isBlank(requestLineAndHeadersString)) {
            LOGGER.debug("empty request");
            return;
        }

        // 解析method、uri、version、queryString
        String firstLine = requestLineAndHeadersString.lines().findFirst().orElseThrow();
        String[] firstLineProperties = firstLine.split(" ");
        method = firstLineProperties[0];
        requestURI = firstLineProperties[1];
        int uriSplitIndex = requestURI.indexOf('?');
        if (uriSplitIndex != -1) {
            queryString = requestURI.substring(uriSplitIndex + 1);
            requestURI = requestURI.substring(0, uriSplitIndex);
        }

        // 解析http请求的header
        requestLineAndHeadersString.lines().skip(1)
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

    public String readRequestBodyAsString(int contentLength) throws IOException {

        final byte[] bytes = requestStream.readRequestBody(contentLength);
        // java 11的new String()貌似会按照System.getProperty("file.encoding")指定的字符集来解码，在Ubuntu下直接使用new String(bytes)没有问题
        // Windows 10中文版System.getProperty("file.encoding")拿到的是GBK，客户端(浏览器)发送的请求是UTF-8，直接new String(bytes)会乱码
        // 考虑到字符集兼容性问题，将客户端(浏览器)请求、源代码文件、服务端响应的字符集统一成UTF-8
        requestBodyString = new String(bytes, Constants.Server.DEFAULT_CHARSET);
        return requestBodyString;
    }

    /**
     * 解析请求参数，输入参数字符串(样例id=1&name=ccj)，将参数解析出来以键值对形式存入parameters数据成员。
     * @param parameterString 请求的参数，包括url参数和application/x-www-form-urlencoded请求体里的参数，格式样例为id=1&name=ccj
     */
    public void parseParameter(String parameterString) {

        if (StringUtils.isBlank(parameterString)) {
            return;
        }

        AMP_SPLITTER.split(parameterString).forEach(s -> {
            int equalsSignIndex = s.indexOf('=');
            String name = s.substring(0, equalsSignIndex);
            String value = s.substring(equalsSignIndex + 1);
            parameters.put(name, value);
        });
    }

    public String getRequestBodyString() {
        return requestBodyString;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public String getHeader(String name) {
        return ObjectUtils.defaultIfNull(headers.get(name), headers.get(name.toLowerCase()));
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
    public int getContentLength() {
        return MapUtils.getIntValue(headers, HttpHeaders.CONTENT_LENGTH, -1);
    }

    @Override
    public String getContentType() {
        return getHeader(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public String getParameter(String name) {
        return Optional.ofNullable(parameters.get(name))
                .flatMap(values -> values.stream().findFirst())
                .orElse(null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return Optional.ofNullable(parameters.get(name))
                .map(list -> Iterables.toArray(list, String.class))
                .orElse(new String[0]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new HashMap<>();
        parameters.asMap()
                .forEach((key, collection) -> result.put(key, Iterables.toArray(collection, String.class)));
        return result;
    }
}
