package com.stinjoss.springbootmvc.app.controllers;

import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.exceptions.BusinessLogicException;
import com.stinjoss.springbootmvc.app.services.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/process-login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserResponseDTO usuarioLogueado = userService.login(username, password);
            session.setAttribute("usuarioLogueado", usuarioLogueado);
            return "redirect:/dashboard";
        } catch (BusinessLogicException e) {
            // Si el servicio lanza una excepción de negocio (usuario/pass incorrecto, cuenta inactiva)
            // la capturamos aquí.
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            // Para cualquier otro error inesperado
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado. Intente más tarde.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("logout", "Has cerrado sesión exitosamente.");
        return "redirect:/auth/login";
    }
}
