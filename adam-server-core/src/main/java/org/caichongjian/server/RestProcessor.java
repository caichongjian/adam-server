package org.caichongjian.server;

import com.alibaba.fastjson.JSON;
import org.caichongjian.annotations.MiniRequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RestProcessor {

    private static final Map<String, RestMethodInvoker> URI_MAPPING = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RestProcessor.class);

    /**
     * Process servlet request.
     *
     * @param request  http request
     * @param response http response
     */
    public void process(Request request, Response response) throws IOException {
        RestMethodInvoker restMethodInvoker = URI_MAPPING.get(request.getUri());
        try {
            final Object returnValue = restMethodInvoker.invoke(request, response);
            String jsonString = JSON.toJSONString(returnValue);
            response.sendJsonString(jsonString);
        } catch (Exception e) {
            LOGGER.error("请求处理失败", e);
            // TODO 请求处理失败时，提示得更人性化
            Map<String, Object> message = Map.of("success", false, "msg", "操作失败");
            response.sendJsonString(JSON.toJSONString(message));
        }
    }

    /**
     * 添加uri和方法的映射
     */
    public static void addUriMapping(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final MiniRequestMapping typeAnnotation = clazz.getAnnotation(MiniRequestMapping.class);
        final Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            final MiniRequestMapping methodAnnotation = method.getAnnotation(MiniRequestMapping.class);
            if (methodAnnotation != null) {
                // TODO 考虑配置为空的情况
                String uri = typeAnnotation.value() + methodAnnotation.value();
                Object instance = clazz.getConstructor().newInstance();
                URI_MAPPING.put(uri, new RestMethodInvoker(method, instance));
            }
        }
    }

    /**
     * 判断RestProcessor是否包含了uri的映射。
     * 如果没包含，只能当做静态资源请求处理了
     *
     * @param uri uri
     * @return 是否包含了uri的映射
     */
    public static boolean containsUriMapping(String uri) {
        // TODO 考虑uri多个斜杠少个斜杠的情况
        return URI_MAPPING.containsKey(uri);
    }
}
