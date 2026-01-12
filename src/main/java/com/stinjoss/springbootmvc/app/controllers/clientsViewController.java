package com.stinjoss.springbootmvc.app.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class clientsViewController {


    @GetMapping
    public String listProducts() {
        return "listClients";
    }

    @GetMapping("/FormClients")
    public String formCreate() {
        return "FormClients";
    }

    @GetMapping("/FormClients/{id}")
    public String formUpdate() {
        return "FormClients";
    }
}
