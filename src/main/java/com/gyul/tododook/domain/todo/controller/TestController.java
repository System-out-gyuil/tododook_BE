package com.gyul.tododook.domain.todo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
//    test3
    @GetMapping("/")
    public String home() {
        return "ok!!!!!!!!!!!!! please333333333332";
    }
}
