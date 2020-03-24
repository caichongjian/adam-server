package org.caichongjian.server;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.caichongjian.annotations.MiniRequestBody;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.Response;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 用来调用rest方法，与uri是一对一关系。
 * Q: 为什么要有RestMethodInvoker？
 * A: 想要通过反射调用restController里定义的方法，必须要有Method和restController实例。
 * 将这两者封装成RestMethodInvoker后，即可在RestProcessor中通过URI_MAPPING路由到restController里定义的方法
 */
public class RestMethodInvoker {

    private Method method;
    private Object instance;
    private Argument[] argumentDefinitions;

    private static final class Argument {
        private String name;
        private Class<?> type;
        private boolean isRequestBodyArgument;

        public Argument(String name, Class<?> type, boolean isRequestBodyArgument) {
            this.name = name;
            this.type = type;
            this.isRequestBodyArgument = isRequestBodyArgument;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public boolean isRequestBodyArgument() {
            return isRequestBodyArgument;
        }
    }

    /**
     * 参数转换函数。输入字符串和需要转换的类型，返回转换后的对象
     */
    private static final BiFunction<String, Class<?>, Object> parseParameterFunction = (str, clazz) -> {
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
    };

    public RestMethodInvoker(Method method, Object instance) {
        this.method = method;
        this.instance = instance;

        // 方法的各个参数的名称、类型、是否请求体参数数据没必要每次请求过来时都通过反射获取，将它们缓存到arguments这一成员变量中,从而提高性能
        final Parameter[] parameterDefinitions = method.getParameters();
        argumentDefinitions = new Argument[parameterDefinitions.length];
        for (int i = 0; i < parameterDefinitions.length; i++) {
            final Parameter definition = parameterDefinitions[i];
            argumentDefinitions[i] = new Argument(definition.getName(), definition.getType(), definition.getAnnotation(MiniRequestBody.class) != null);
        }
    }

    public Object invoke(Request request, Response response) throws InvocationTargetException, IllegalAccessException, IOException {

        // 解析url的参数(静态资源不需要解析参数，所以解析参数代码放这里而不是和解析请求头代码放一起)
        Optional.ofNullable(request.getQueryString())
                .map(queryString -> URLDecoder.decode(queryString, Charset.defaultCharset()))
                .ifPresent(request::parseParameter);

        // 解析请求体的参数
        final String contentType = request.getContentType();
        final int contentLength = request.getContentLength();
        if (contentLength > 0 && shouldReadBodyAsString(contentType)) {
            String requestBody = request.readRequestBodyAsString(contentLength);
            if (contentTypeEquals(contentType, Constants.ContentType.APPLICATION_FORM_URLENCODED)) {
                request.parseParameter(requestBody);
            }
        }

        // 根据方法定义中的参数列表，从request中获取相应的参数
        final Object[] parameters = new Object[argumentDefinitions.length];
        for (int i = 0; i < argumentDefinitions.length; i++) {

            Argument argumentDefinition = argumentDefinitions[i];
            Class<?> clazz = argumentDefinition.getType();

            if (clazz == MiniHttpServletRequest.class) {
                parameters[i] = request;
            } else if (clazz == MiniHttpServletResponse.class) {
                parameters[i] = response;
            } else if (clazz.isArray()) {
                String[] parameterStringArray = request.getParameterValues(argumentDefinition.getName());
                parameters[i] = parseParameter(clazz, parameterStringArray);
            } else if (contentTypeEquals(contentType, Constants.ContentType.APPLICATION_JSON) && argumentDefinition.isRequestBodyArgument()) {
                parameters[i] = Optional.ofNullable(request.getRequestBodyString())
                        .map(s -> JSON.parseObject(s, clazz))
                        .orElse(null);
            } else {
                String parameterString = request.getParameter(argumentDefinition.getName());
                parameters[i] = Optional.ofNullable(parameterString)
                        .map(str -> parseParameterFunction.apply(str, clazz)).orElse(null);
            }
        }
        return method.invoke(instance, parameters);
    }

    /**
     * 将参数转换成指定的类型
     *
     * @param parameterStrings String类型的参数数组
     * @param clazz            需要转换成的类型
     * @return 转换后的对象
     */
    private Object parseParameter(Class<?> clazz, String[] parameterStrings) {

        return Optional.ofNullable(parameterStrings)
                .map(strings -> {
                    final Class<?> componentType = clazz.getComponentType();
                    Object parameterArray = Array.newInstance(componentType, strings.length);
                    for (int i = 0; i < strings.length; i++) {
                        String parameterString = strings[i];
                        Array.set(parameterArray, i, parseParameterFunction.apply(parameterString, componentType));
                    }
                    return parameterArray;
                }).orElse(Array.newInstance(clazz.getComponentType(), 0));
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
}
