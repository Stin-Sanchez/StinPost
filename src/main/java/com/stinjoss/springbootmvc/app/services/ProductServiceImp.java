package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import com.stinjoss.springbootmvc.app.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImp implements ProductService {

    private final ProductRepository repository;

    @Value("${upload.dir}") // Inyectamos la ruta desde application.properties
    private String uploadDir;

    public ProductServiceImp(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsResponseDTO> findAll() {
        return this.repository.findByActiveTrue().stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductsResponseDTO> findById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductsResponseDTO> buscarPorTermino(String termino) {
        if (termino == null || termino.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.buscarPorCodigoONombre(termino).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    @Override
    public List<ProductsResponseDTO> findByState(String state) {
        List<Products> lista;

        // 1. Si viene vacío, nulo o dice "Todos", devolvemos todo
        if (state == null || state.isEmpty() || state.equals("Todos")) {
            lista = repository.findAll();
        } else {
            try {
                // 2. Convertimos el String (ej: "AGOTADO") al Enum real
                StatesProducts estadoEnum = StatesProducts.valueOf(state);
                lista = repository.findByState(estadoEnum);
            } catch (IllegalArgumentException e) {
                // Si mandan un estado que no existe, devolvemos lista vacía
                return Collections.emptyList();
            }
        }

        // 3. Mapeamos a DTO
        return lista.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ProductsResponseDTO save(ProductRequestDTO request, MultipartFile imageFile, Long id) {
        Products productDB;

        // 1. LÓGICA DE EDICIÓN (UPDATE)
        if (id != null && id > 0) {
            // Buscamos el producto. Si no existe, lanzamos error controlado.
            productDB = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No se encontró el producto con ID: " + id));

        } else {
            productDB = new Products();
            productDB.setActive(true);
        }

        // --- MAPEO DE CAMPOS COMUNES (DTO -> ENTIDAD) ---
        // Estos datos se actualizan tanto al crear como al editar
        productDB.setCode(request.getCode());
        productDB.setNameProducto(request.getNameProducto());
        productDB.setDescription(request.getDescription());
        productDB.setMarca(request.getMarca());
        productDB.setPrice(request.getPrice());
        productDB.setStock(request.getStock());
        productDB.setMinStock(request.getMinStock());
        productDB.setState(request.getState());
        productDB.setExpirationDate(request.getExpirationDate());


        // --- MANEJO DE IMAGEN EN EDICIÓN ---
        // Solo cambiamos la imagen si el usuario subió una NUEVA
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = guardarArchivo(imageFile);
            productDB.setImage(imagePath);
        }

        Products saveProduct = repository.save(productDB);
        return mapToResponse(saveProduct);
    }

    // --- MÉTODO AUXILIAR PRIVADO PARA GUARDAR EN DISCO ---
    private String guardarArchivo(MultipartFile file) {
        try {
            // Generar nombre único: "uuid_nombrefoto.jpg"
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Ruta absoluta
            Path rootPath = Paths.get(uploadDir);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }

            // Guardar bytes
            Files.copy(file.getInputStream(), rootPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            // Retornar la ruta web relativa (lo que se guarda en BD)
            // Asegúrate que coincida con tu WebConfig: /uploads/products/...
            return "/uploads/products/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar imagen: " + e.getMessage());
        }
    }

    @Override
    public Optional<ProductsResponseDTO> delete(Long id) {
        //Buscamos el producto
        Optional<Products> productsOptional = repository.findById(id);
        if (productsOptional.isPresent()) {
            Products product = productsOptional.get();
            product.setActive(false);
            product.setState(StatesProducts.AGOTADO);
            repository.save(product);
            return productsOptional.map(this::mapToResponse);
        }
        return Optional.empty();
    }

    @Override
    public Long count() {
        return repository.count();
    }


    //Mapper
    private ProductsResponseDTO mapToResponse(Products entity) {
        ProductsResponseDTO dto = new ProductsResponseDTO();
        dto.setId(entity.getId());
        dto.setNameProducto(entity.getNameProducto());
        dto.setDescription(entity.getDescription());
        dto.setMarca(entity.getMarca());
        dto.setCode(entity.getCode());
        dto.setState(entity.getState());
        dto.setStock(entity.getStock());
        dto.setMinStock(entity.getMinStock());
        dto.setPrice(entity.getPrice());
        dto.setImage(entity.getImage());
        dto.setExpirationDate(entity.getExpirationDate());
        return dto;
    }
}
