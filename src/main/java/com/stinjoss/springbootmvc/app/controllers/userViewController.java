package com.stinjoss.springbootmvc.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")//raíz del controller

public class userViewController {

    @GetMapping
    public String listProducts() {
        return "list";
    }

    @GetMapping("/form")
    public String formCreate() {
        return "form";
    }

    @GetMapping("/form/{id}")
    public String formUpdate() {
        return "form";
    }
}
