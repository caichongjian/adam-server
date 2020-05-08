package org.caichongjian.server;

import org.caichongjian.annotations.MiniRequestBody;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 用来调用rest方法，与uri是一对一关系。
 * Q: 为什么要有RestMethodInvoker？
 * A: 想要通过反射调用restController里定义的方法，必须要有Method和restController实例。
 * 将这两者封装成RestMethodInvoker后，即可在RestProcessor中通过URI_MAPPING路由到restController里定义的方法
 */
public class RestMethodInvoker {

    private final Method method;
    private final Object instance;
    private final Argument[] argumentDefinitions;

    private static final class Argument {
        private final String name;
        private final Class<?> type;
        private final boolean isRequestBodyArgument;

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

    public RestMethodInvoker(Method method, Object instance) {
        this.method = method;
        this.instance = instance;

        // 方法的各个参数的名称、类型、是否请求体参数数据没必要每次请求过来时都通过反射获取，将它们缓存到argumentDefinitions这一成员变量中,从而提高性能
        final Parameter[] parameterDefinitions = method.getParameters();
        argumentDefinitions = new Argument[parameterDefinitions.length];
        for (int i = 0; i < parameterDefinitions.length; i++) {
            final Parameter definition = parameterDefinitions[i];
            argumentDefinitions[i] = new Argument(definition.getName(), definition.getType(), definition.getAnnotation(MiniRequestBody.class) != null);
        }
    }

    public Object invoke(Request request, Response response) throws InvocationTargetException, IllegalAccessException, IOException {

        // 解析请求体
        request.parseRequestBody();

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
                parameters[i] = request.getParameterValues(argumentDefinition.getName(), clazz);
            } else if (argumentDefinition.isRequestBodyArgument()) {
                parameters[i] = request.getJsonBody(clazz);
            } else {
                parameters[i] = request.getParameter(argumentDefinition.getName(), clazz);
            }
        }
        return method.invoke(instance, parameters);
    }
}
