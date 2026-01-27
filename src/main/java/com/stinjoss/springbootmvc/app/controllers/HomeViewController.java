package com.stinjoss.springbootmvc.app.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.DashboardStatsDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.services.SalesService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final SalesService salesService;
    private final ObjectMapper objectMapper; // Inyectamos Jackson

    @GetMapping({"/", "/dashboard"})
    public String home(HttpSession session, Model model) {
        // Validación de seguridad
        UserResponseDTO user = (UserResponseDTO) session.getAttribute("usuarioLogueado");
        if (user == null) {
            return "redirect:/auth/login"; // Lo mandamos al login si no hay sesión
        }

        // Obtenemos las estadísticas reales
        DashboardStatsDTO stats = salesService.getDashboardStats();

        // Si pasa, mostramos la página
        model.addAttribute("usuario", user);
        model.addAttribute("stats", stats);
        
        // Convertimos el objeto chartData a JSON String aquí en el controlador
        try {
            String chartDataJson = objectMapper.writeValueAsString(stats.getSalesChart());
            model.addAttribute("chartDataJson", chartDataJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("chartDataJson", "{}");
            e.printStackTrace();
        }

        return "dashboard"; // tu archivo home.html
    }
}