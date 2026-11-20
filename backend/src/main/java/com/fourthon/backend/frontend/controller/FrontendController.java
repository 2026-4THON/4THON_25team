package com.fourthon.backend.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    private static final String INDEX = "forward:/index.html";

    @GetMapping({
            "/start",
            "/map",
            "/my",
            "/search",
            "/explore",
            "/record/**",
            "/analysis/**",
            "/detail/**"
    })
    public String forward() {
        return INDEX;
    }
}
