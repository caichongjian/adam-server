package org.caichongjian.example.web;

import org.caichongjian.annotations.MiniRequestMapping;
import org.caichongjian.annotations.MiniRestController;

import java.util.Map;

@MiniRestController
@MiniRequestMapping("/example")
public class ExampleController {

    @MiniRequestMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of("ccj", 1, "adam", 2);
    }
}
