package com.stinjoss.springbootmvc.app.controllers.API;


import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ClientRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ClientResponseDTO;
import com.stinjoss.springbootmvc.app.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class clientRestController {

    @Autowired
    private final ClientService clientService;

    // 1. LISTAR TODOS
    // Response: Enviamos DTOs al front para que pinte la lista
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> listClients() {
        return ResponseEntity.ok(clientService.findAll());
    }

    //Busca por id
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> details(@PathVariable Long id) {
        return clientService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    // 3. CREAR
    // Request (Entrada): Datos del formulario
    // Response (Salida): El cliente creado con su ID nuevo
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO client) {
        ClientResponseDTO neWClient = clientService.save(client, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(neWClient);
    }

    // 5. EDITAR
    // Request: Datos a modificar
    // Response: Cómo quedó el cliente final
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO client) {
        // Solo aseguramos que el ID del objeto coincida con el de la URL
        ClientResponseDTO clienteEdit = clientService.save(client, id);
        // Llamamos al MISMO método save. Él sabrá que es una edición por el ID.
        return ResponseEntity.ok(clienteEdit);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> delete(@PathVariable Long id) {
        return clientService.delete(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/search/{term}")
    public List<ClientResponseDTO> buscar(@PathVariable String term) {
        return clientService.findByFullNameOrDni(term); //busca por nombre completo o dni
    }

}


