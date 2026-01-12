package com.stinjoss.springbootmvc.app.controllers.API;


import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.UserRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class userRestController {


    @Autowired
    private final UserService service;
    
    // 1. LISTAR TODOS
    // Response: Enviamos DTOs al front para que pinte la lista
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(service.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> details(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 3. CREAR
    // Request (Entrada): Datos del formulario
    // Response (Salida): El usuario creado con su ID nuevo
    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO user) {
        UserResponseDTO newUser = service.save(user, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // 4. BUSCAR
    // Response: Lista de DTOs que coinciden
    @GetMapping("/search/{term}")
    public List<UserResponseDTO> buscar(@PathVariable String term) {
        return service.findByUsernameOrDni(term); // Busca por nombre O código
    }

    // 5. EDITAR
    // Request: Datos a modificar
    // Response: Cómo quedó el usuario final
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO user) {
        // Solo aseguramos que el ID del objeto coincida con el de la URL
        UserResponseDTO userEdit = service.save(user, id);
        // Llamamos al MISMO método save. Él sabrá que es una edición por el ID.
        return ResponseEntity.ok(userEdit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
