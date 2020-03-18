package org.caichongjian.example.web;

import com.google.common.collect.Lists;
import org.caichongjian.annotations.MiniRequestBody;
import org.caichongjian.annotations.MiniRequestMapping;
import org.caichongjian.annotations.MiniRestController;
import org.caichongjian.api.MiniHttpServletRequest;
import org.caichongjian.example.pojo.User;

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
}
