package com.stinjoss.springbootmvc.app.controllers.API;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.SalesResponseDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.services.SalesService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/sales")
@RequiredArgsConstructor
public class salesRestCOntroller {

    @Autowired
    private final SalesService service;

    // 1. OBTENER TODAS LAS VENTAS
    @GetMapping
    public ResponseEntity<List<SalesResponseDTO>> list(@RequestParam(required = false) String state) {
        return ResponseEntity.ok(service.findByState(state));
    }

    // 2. OBTENER VENTA POR ID
    @GetMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> show(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{term}")
    public List<SalesResponseDTO> buscar(@PathVariable String term) {
        return service.findByTerm(term);
    }


    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SalesRequestDTO saleRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>(); // Nuestro mapa de respuesta

        try {

            //Recuperamos al usuario logueado el cual sera el responsable de realizar las ventas
            UserResponseDTO sellerLogin = (UserResponseDTO) session.getAttribute("usuarioLogueado");

            if (sellerLogin == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión");
            }

            SalesResponseDTO newSale = service.save(saleRequest, sellerLogin.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(newSale);

        } catch (RuntimeException e) {
            // Error de Negocio (Stock insuficiente, cliente no existe, etc.)
            response.put("éxito", false);
            response.put("mensaje", "No se pudo procesar la venta");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());


            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            // Error Crítico (Base de datos caída, NullPointer, etc.)
            e.printStackTrace(); // Importante para ver el error en consola

            response.put("éxito", false);
            response.put("mensaje", "Error interno del servidor");
            response.put("error", e.getMessage()); // Opcional: Ocultar en producción
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 4. ELIMINAR VENTA
    @DeleteMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> delete(@PathVariable Long id) {
        return service.anularVenta(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

    }

}
