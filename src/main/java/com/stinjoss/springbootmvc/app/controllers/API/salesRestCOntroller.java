package com.stinjoss.springbootmvc.app.controllers.API;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.DashboardStatsDTO;
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

import java.util.List;

@RestController
@RequestMapping("api/sales")
@RequiredArgsConstructor
public class salesRestCOntroller {

    @Autowired
    private final SalesService service;

    @GetMapping
    public ResponseEntity<List<SalesResponseDTO>> list(@RequestParam(required = false) String state) {
        return ResponseEntity.ok(service.findByState(state));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> show(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/search/{term}")
    public List<SalesResponseDTO> buscar(@PathVariable String term) {
        return service.findByTerm(term);
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }

    @PostMapping
    public ResponseEntity<SalesResponseDTO> create(@Valid @RequestBody SalesRequestDTO saleRequest, HttpSession session) {
        UserResponseDTO sellerLogin = (UserResponseDTO) session.getAttribute("usuarioLogueado");

        if (sellerLogin == null) {
            throw new RuntimeException("Debe iniciar sesión para realizar una venta");
        }

        SalesResponseDTO newSale = service.save(saleRequest, sellerLogin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newSale);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularVenta(@PathVariable Long id) {
        service.anularVenta(id);
        return ResponseEntity.noContent().build();
    }
}
