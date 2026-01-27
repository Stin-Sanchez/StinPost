package com.stinjoss.springbootmvc.app.controllers.API;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import com.stinjoss.springbootmvc.app.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 1. LISTAR TODOS (PAGINADO)
    // Ejemplo: /api/products?page=0&size=10&state=DISPONIBLE
    @GetMapping()
    public ResponseEntity<Page<ProductsResponseDTO>> filterByState(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(service.findByState(state, pageable));
    }


    // 2. DETALLE POR ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductsResponseDTO> details(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    // 3. CREAR
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> create(
            @Valid @ModelAttribute ProductRequestDTO product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductsResponseDTO nuevo = service.save(product, imageFile, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 4. BUSCAR (PAGINADO)
    @GetMapping("/search/{term}")
    public ResponseEntity<Page<ProductsResponseDTO>> buscar(
            @PathVariable String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.buscarPorTermino(term, pageable));
    }


    // 5. EDITAR
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequestDTO product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductsResponseDTO editado = service.save(product, imageFile, id);
        return ResponseEntity.ok(editado);
    }

    // 6. ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductsResponseDTO> delete(@PathVariable Long id) {
        return service.delete(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
