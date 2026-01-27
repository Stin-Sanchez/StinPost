package com.stinjoss.springbootmvc.app.controllers;

import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")//raíz del controller
public class productViewController {

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("estados", StatesProducts.values());
        return "listProducts";
    }

    @GetMapping("/formProducts")
    public String formCreate(Model model) {
        // Es buena práctica pasar los estados también en la creación
        model.addAttribute("estados", StatesProducts.values());
        return "formProducts";
    }

    // CORRECCIÓN: La ruta ahora coincide con el enlace del frontend
    @GetMapping("/edit/{id}")
    public String formUpdate(@PathVariable Long id, Model model) {
        // Pasamos los estados para que el <select> se renderice correctamente
        model.addAttribute("estados", StatesProducts.values());
        // El id no se usa aquí, pero es necesario para que Spring mapee la ruta.
        // El JS se encargará de cargar los datos usando este ID.
        return "formProducts";
    }
}
