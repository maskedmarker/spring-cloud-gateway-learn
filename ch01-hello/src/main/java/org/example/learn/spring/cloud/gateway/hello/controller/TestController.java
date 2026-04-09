package org.example.learn.spring.cloud.gateway.hello.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Gateway Test Controller!";
    }

    @GetMapping("/info")
    public String info() {
        return "This is a test endpoint for Spring Gateway demo";
    }
}