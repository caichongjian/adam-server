package org.caichongjian.example.web;

import com.google.common.collect.Lists;
import org.caichongjian.annotations.MiniRequestBody;
import org.caichongjian.annotations.MiniRequestMapping;
import org.caichongjian.annotations.MiniRestController;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.api.MiniHttpServletResponse;
import org.caichongjian.example.pojo.User;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MiniRestController
@MiniRequestMapping("/example")
public class ExampleController {

    @MiniRequestMapping("/hello")
    public Map<String, Object> hello(Integer id, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("author", "蔡崇建");
        return map;
    }

    @MiniRequestMapping("/hi")
    public Map<String, String[]> hi(MiniHttpServletRequest request) {
        return request.getParameterMap();
    }

    @MiniRequestMapping("/parameter/array")
    public Map<String, Object> arrayParameter(long[] ids) {
        Map<String, Object> map = new HashMap<>();
        map.put("ids", ids);
        return map;
    }

    @MiniRequestMapping("/json")
    public List<User> json(@MiniRequestBody User user) {
        return Lists.newArrayList(user);
    }

    @MiniRequestMapping("/cookie")
    public Map<String, Object> cookie(MiniHttpServletResponse response) {
        Cookie cookie = new Cookie("a_my_name", "ccj");
        cookie.setPath("/");
        response.addCookie(cookie);

        cookie = new Cookie("a_my_age", "28");
        response.addCookie(cookie);

        cookie = new Cookie("a_qwertyuiop", "asdfghjklzxcvbnm");
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(1800);
        cookie.setSecure(true);
        cookie.setVersion(1);
        cookie.setComment("逗你玩");
        response.addCookie(cookie);
        return Map.of("success", true);
    }
}
