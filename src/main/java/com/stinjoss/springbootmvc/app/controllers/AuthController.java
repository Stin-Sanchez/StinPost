package com.stinjoss.springbootmvc.app.controllers;

import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
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


@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserService userService;


    //Mostrar el login
    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        // Si ya está logueado, redirigir al home
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/process-login")
    //Procesar login
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {

        //Buscar usuario en bd
        UserResponseDTO usuarioLogueado = userService.login(username, password);

        if (usuarioLogueado != null) {
            session.setAttribute("usuarioLogueado", usuarioLogueado);
            return "redirect:/dashboard";
        }
        // 3. Error
        model.addAttribute("error", "Credenciales incorrectas");
        return "login";

    }

    // 3. Cerrar Sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout";
    }

}
