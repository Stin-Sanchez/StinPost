package com.stinjoss.springbootmvc.app.controllers.API;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import com.stinjoss.springbootmvc.app.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class productRestController {

    @Autowired
    private ProductService service;

    // 1. LISTAR TODOS
    // Response: Enviamos DTOs al front para que pinte la lista
    @GetMapping()
    public ResponseEntity<List<ProductsResponseDTO>> filterByStata(@RequestParam(required = false) String state) {
        return ResponseEntity.ok(service.findByState(state));
    }


    // 2. DETALLE POR ID
    // Response: Enviamos un solo DTO con todos los detalles

    @GetMapping("/{id}")
    public ResponseEntity<ProductsResponseDTO> details(@PathVariable Long id) {
        // El service ya debe devolver Optional<ProductResponseDTO>
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    // 3. CREAR
    // Request (Entrada): Datos del formulario (ProductRequestDTO)
    // Response (Salida): El producto creado con su ID nuevo (ProductResponseDTO)

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> create(
            @Valid @ModelAttribute ProductRequestDTO product,//valida inputs
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile // Recibe el archivo
    ) {
        // Pasamos null en el ID porque es creación
        ProductsResponseDTO nuevo = service.save(product, imageFile, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 4. BUSCAR
    // Response: Lista de DTOs que coinciden

    @GetMapping("/search/{term}")
    public List<ProductsResponseDTO> buscar(@PathVariable String term) {
        return service.buscarPorTermino(term); // Busca por nombre O código
    }


    // 5. EDITAR
    // Request: Datos a modificar
    // Response: Cómo quedó el producto final
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequestDTO product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        // Pasamos el ID de la URL para que el service sepa cuál actualizar
        ProductsResponseDTO editado = service.save(product, imageFile, id);
        return ResponseEntity.ok(editado);
    }

    // 6. ELIMINAR
    // Response: Devolvemos el DTO eliminado (opcional) o solo status OK
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductsResponseDTO> delete(@PathVariable Long id) {
        // El service.delete(id) debe devolver Optional<ProductResponseDTO>
        return service.delete(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
