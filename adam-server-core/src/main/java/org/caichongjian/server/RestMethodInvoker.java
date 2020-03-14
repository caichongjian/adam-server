package org.caichongjian.server;

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

    public RestMethodInvoker(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    public Object invoke(Request request, Response response) throws InvocationTargetException, IllegalAccessException {
        // TODO 考虑请求参数等情况
        return method.invoke(instance);
    }
}
