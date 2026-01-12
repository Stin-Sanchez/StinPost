package com.stinjoss.springbootmvc.app.controllers;

import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sales")//raíz del controller

public class salesViewController {

    @GetMapping
    public String listSales(Model model) {
        model.addAttribute("estados", StatesSales.values());
        return "listSales";
    }

    @GetMapping("/formSales")
    public String formCreate() {
        return "formSales";
    }

    @GetMapping("/formSales/{id}")
    public String formUpdate(Model model) {
        model.addAttribute("estados", StatesSales.values());
        return "formSales";

    }

}
