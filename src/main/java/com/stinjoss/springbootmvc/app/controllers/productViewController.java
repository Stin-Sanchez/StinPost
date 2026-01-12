package com.stinjoss.springbootmvc.app.controllers;

import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")//raíz del controller
public class productViewController {

    @GetMapping
    public String listProducts(Model model) {

        //Enviamos todos los valores del enum con cada estado de producto
        model.addAttribute("estados", StatesProducts.values());
        return "listProducts";
    }

    @GetMapping("/formProducts")
    public String formCreate() {
        return "formProducts";
    }

    @GetMapping("/formProducts/{id}")
    public String formUpdate(Model model) {
        model.addAttribute("estados", StatesProducts.values());
        return "formProducts";
    }

}

