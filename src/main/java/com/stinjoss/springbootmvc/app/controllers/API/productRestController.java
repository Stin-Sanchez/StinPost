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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class productRestController {

    @Autowired
    private ProductService service;

    @GetMapping()
    public ResponseEntity<Page<ProductsResponseDTO>> listProducts(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductsResponseDTO> resultPage;
        
        if (state != null && !state.isEmpty() && !state.equalsIgnoreCase("ALL")) {
            // Si se especifica un estado, filtramos por él
            resultPage = service.findByState(state, pageable);
        } else {
            // Si no, buscamos todos los productos activos
            resultPage = service.findAll(pageable);
        }
        
        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductsResponseDTO> details(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> create(
            @Valid @ModelAttribute ProductRequestDTO product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductsResponseDTO nuevo = service.save(product, imageFile, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping("/search/{term}")
    public ResponseEntity<Page<ProductsResponseDTO>> buscar(
            @PathVariable String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.buscarPorTermino(term, pageable));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequestDTO product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        ProductsResponseDTO editado = service.save(product, imageFile, id);
        return ResponseEntity.ok(editado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
