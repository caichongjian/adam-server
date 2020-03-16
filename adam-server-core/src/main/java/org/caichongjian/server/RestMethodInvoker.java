package org.caichongjian.server;

import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        // TODO 考虑请求参数等情况
        String requestBody = request.readRequestBodyAsString();
        LOGGER.debug(requestBody);
        return method.invoke(instance);
    }
}
