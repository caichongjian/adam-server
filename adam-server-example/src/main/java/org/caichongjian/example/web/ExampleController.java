package org.caichongjian.example.web;

import org.caichongjian.annotations.MiniRequestMapping;
import org.caichongjian.annotations.MiniRestController;
import org.caichongjian.api.MiniHttpServletRequest;

import java.util.HashMap;
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
}
