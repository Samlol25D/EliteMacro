package com.example.EliteMacro.elitemacro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<meta http-equiv='refresh' content='0; URL=home.html'>";
    }
}

