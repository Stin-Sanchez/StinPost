package com.stinjoss.springbootmvc.app.controllers;


import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

    @GetMapping({"/", "/dashboard"})
    public String home(HttpSession session, Model model) {
        // Validación de seguridad
        UserResponseDTO user = (UserResponseDTO) session.getAttribute("usuarioLogueado");
        if (user == null) {
            return "redirect:/auth/login"; // Lo mandamos al login si no hay sesión
        }

        // Si pasa, mostramos la página
        model.addAttribute("usuario", user);
        return "dashboard"; // tu archivo home.html
    }
}