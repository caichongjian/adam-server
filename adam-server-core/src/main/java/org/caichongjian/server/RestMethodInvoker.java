package org.caichongjian.server;

import org.apache.commons.lang3.StringUtils;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.server.http.Constants;
import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * 用来调用rest方法，与uri是一对一关系。
 * Q: 为什么要有RestMethodInvoker？
 * A: 想要通过反射调用restController里定义的方法，必须要有Method和restController实例。
 * 将这两者封装成RestMethodInvoker后，即可在RestProcessor中通过URI_MAPPING路由到restController里定义的方法
 */
public class RestMethodInvoker {

    private Method method;
    private Object instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestMethodInvoker.class);

    public RestMethodInvoker(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    public Object invoke(Request request, Response response) throws InvocationTargetException, IllegalAccessException, IOException {

        // 解析url的参数(静态资源不需要解析参数，所以解析参数代码放这里而不是和解析请求头代码放一起)
        String queryString = request.getQueryString();
        queryString = URLDecoder.decode(queryString, Charset.defaultCharset());
        request.parseParameter(queryString);

        // 解析请求体的参数
        final String contentType = request.getContentType();
        final int contentLength = request.getContentLength();
        if (contentLength > 0 && shouldReadBodyAsString(contentType)) {
            String requestBody = request.readRequestBodyAsString(contentLength);
            LOGGER.debug(requestBody);
            request.parseParameter(requestBody);
        }

        // 根据方法定义中的参数列表，从request中获取相应的参数
        final Parameter[] parameterDefinitions = method.getParameters();
        final Object[] parameters = new Object[parameterDefinitions.length];
        for (int i = 0; i < parameterDefinitions.length; i++) {
            Parameter parameterDefinition = parameterDefinitions[i];

            Class<?> clazz = parameterDefinition.getType();
            if (clazz == MiniHttpServletRequest.class) {
                parameters[i] = request;
            } else if (clazz == MiniHttpServletResponse.class) {
                parameters[i] = response;
            } else if (clazz.isArray()) {
                // TODO 考虑参数为数组的情况,考虑@RequestBody的情况
            } else {
                String parameterString = request.getParameter(parameterDefinition.getName());
                parameters[i] = parseParameter(parameterString, clazz);
            }
        }
        return method.invoke(instance, parameters);
    }

    /**
     * 将参数转换成指定的类型
     *
     * @param parameterString 字符串类型的参数
     * @param clazz           需要转换成的类型
     * @return 转换后的对象
     */
    private Object parseParameter(String parameterString, Class<?> clazz) {
        return Optional.ofNullable(parameterString)
                .map(str -> {
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
                    return parameter;
                }).orElse(null);
    }

    /**
     * 判断是否需要以字符串读取请求体
     *
     * @param contentType 请求头的contentType
     * @return 是否需要以字符串读取请求体
     */
    private boolean shouldReadBodyAsString(String contentType) {
        return contentTypeEquals(contentType, Constants.APPLICATION_FORM_URLENCODED) ||
                contentTypeEquals(contentType, Constants.APPLICATION_JSON);
    }

    private boolean contentTypeEquals(String contentType, String expected) {
        return StringUtils.startsWithIgnoreCase(contentType, expected);
    }
}
