package org.caichongjian.server.http;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.net.HttpHeaders;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.server.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
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
    private String requestBodyString; // 请求体里的字符串。传json参数全靠它了
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    private static final Splitter AMP_SPLITTER = Splitter.on("&").omitEmptyStrings();

    public Request(RequestStream requestStream) {
        this.requestStream = requestStream;
    }

    /**
     * 解析请求行和请求头
     * 内部方法，在一次请求中只能调用一次，仅供adam-server-core内部调用
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

        // 解析url的参数
        Optional.ofNullable(queryString)
                .map(str -> URLDecoder.decode(str, Constants.Server.DEFAULT_CHARSET))
                .ifPresent(this::parseParameter);
    }

    /**
     * 解析请求体
     * 内部方法，在一次请求中只能调用一次，仅供adam-server-core内部调用
     *
     * @throws IOException IOException
     */
    public void parseRequestBody() throws IOException {

        int contentLength = getContentLength();
        final String contentType = getContentType();
        if (contentLength > 0 && shouldReadBodyAsString(contentType)) {

            final byte[] bytes = requestStream.readRequestBody(contentLength);
            // java 11的new String()貌似会按照System.getProperty("file.encoding")指定的字符集来解码，在Ubuntu下直接使用new String(bytes)没有问题
            // Windows 10中文版System.getProperty("file.encoding")拿到的是GBK，客户端(浏览器)发送的请求是UTF-8，直接new String(bytes)会乱码
            // 考虑到字符集兼容性问题，将客户端(浏览器)请求、源代码文件、服务端响应的字符集统一成UTF-8
            requestBodyString = new String(bytes, Constants.Server.DEFAULT_CHARSET);
            if (contentTypeEquals(contentType, Constants.ContentType.APPLICATION_FORM_URLENCODED)) {
                parseParameter(requestBodyString);
            }
        }
    }

    /**
     * 解析请求参数，输入参数字符串(样例id=1&name=ccj)，将参数解析出来以键值对形式存入parameters数据成员。
     *
     * @param parameterString 请求的参数，包括url参数和application/x-www-form-urlencoded请求体里的参数，格式样例为id=1&name=ccj
     */
    private void parseParameter(String parameterString) {

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

    /**
     * <p>参数转换函数,用于将String类型的http请求参数转换成指定的类型</p>
     * <p>客户端(浏览器)传递的参数经过初次解析后是String类型，而controller参数列表中
     * 则可能是Integer, Long, BigDecimal类型，因此需要有个函数进行参数类型的转换</p>
     * <p></p>
     *
     * @param str   String类型的http请求参数
     * @param clazz 需要转换成的类型,如Integer.class, Long.class等等
     */
    @SuppressWarnings("unchecked")
    private <T> T castParameterType(String str, Class<T> clazz) {
        Object parameter = null;
        if (clazz == String.class) {
            parameter = str;
        } else if (clazz == Integer.class || clazz == int.class) {
            parameter = Integer.valueOf(str);
        } else if (clazz == Long.class || clazz == long.class) {
            parameter = Long.valueOf(str);
        } else if (clazz == Byte.class || clazz == byte.class) {
            parameter = Byte.valueOf(str);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            parameter = Boolean.valueOf(str);
        } else if (clazz == Double.class || clazz == double.class) {
            parameter = Double.valueOf(str);
        } else if (clazz == Float.class || clazz == float.class) {
            parameter = Float.valueOf(str);
        } else if (clazz == Short.class || clazz == short.class) {
            parameter = Short.valueOf(str);
        } else if (clazz == Character.class || clazz == char.class) {
            parameter = str.charAt(0);
        } else if (clazz == BigDecimal.class) {
            parameter = new BigDecimal(str);
        } else if (clazz == BigInteger.class) {
            parameter = new BigInteger(str);
        }
        return (T) parameter;
    }

    /**
     * 判断是否需要以字符串读取请求体
     *
     * @param contentType 请求头的contentType
     * @return 是否需要以字符串读取请求体
     */
    private boolean shouldReadBodyAsString(String contentType) {
        return contentTypeEquals(contentType, Constants.ContentType.APPLICATION_FORM_URLENCODED) ||
                contentTypeEquals(contentType, Constants.ContentType.APPLICATION_JSON);
    }

    private boolean contentTypeEquals(String contentType, String expected) {
        return StringUtils.startsWithIgnoreCase(contentType, expected);
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

    /**
     * <p>获取指定类型的参数</p>
     * <p>客户端(浏览器)传递的参数经过初次解析后是String类型，而用户可能需要手动转换成
     * Integer, Long, BigDecimal类型，因此提供一个重载方法来帮用户节省工作量</p>
     *
     * @param name 参数的名称
     * @param type 参数的类型,如Integer.class, Long.class等等
     * @return 转换后的对象
     */
    public <T> T getParameter(String name, Class<T> type) {
        String parameterString = getParameter(name);
        return Optional.ofNullable(parameterString)
                .map(str -> castParameterType(str, type))
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

    /**
     * <p>获取指定类型的数组参数</p>
     * <p>客户端(浏览器)传递的参数经过初次解析后是String[]类型，而用户可能需要手动转换成
     * Integer[], Long[], BigDecimal[]类型，因此提供一个重载方法来帮用户节省工作量</p>
     *
     * @param name 数组参数的名称
     * @param type 数组参数的类型,如Integer[].class, Long[].class等等
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValues(String name, Class<T> type) {
        String[] parameterStringArray = getParameterValues(name);
        return (T) Optional.ofNullable(parameterStringArray)
                .map(strings -> {
                    final Class<?> componentType = type.getComponentType();
                    Object parameterArray = Array.newInstance(componentType, strings.length);
                    for (int i = 0; i < strings.length; i++) {
                        String parameterString = strings[i];
                        Array.set(parameterArray, i, castParameterType(parameterString, componentType));
                    }
                    return parameterArray;
                }).orElse(Array.newInstance(type.getComponentType(), 0));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new HashMap<>();
        parameters.asMap()
                .forEach((key, collection) -> result.put(key, Iterables.toArray(collection, String.class)));
        return result;
    }

    /**
     * 获取请求体里的json格式传递的对象
     *
     * @param type 类对象
     * @param <T>  类型
     * @return 对象
     */
    public <T> T getJsonBody(Class<T> type) {
        if (!contentTypeEquals(getContentType(), Constants.ContentType.APPLICATION_JSON)) {
            return null;
        }
        return Optional.ofNullable(requestBodyString)
                .map(s -> JSON.parseObject(s, type))
                .orElse(null);
    }
}
