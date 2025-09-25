package com.pettrackerreview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-i18n")
    public String testI18n() {
        return "test-i18n";
    }
}