package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import com.stinjoss.springbootmvc.app.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImp.class);
    private final ProductRepository repository;

    @Value("${upload.dir}") // Inyectamos la ruta desde application.properties
    private String uploadDir;

    public ProductServiceImp(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductsResponseDTO> findAll(Pageable pageable) {
        log.info("Buscando todos los productos activos, página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Products> productsPage = repository.findByActiveTrue(pageable);
        log.info("Encontrados {} productos en total, {} en esta página.", productsPage.getTotalElements(), productsPage.getNumberOfElements());
        return productsPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductsResponseDTO> findById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductsResponseDTO> buscarPorTermino(String termino, Pageable pageable) {
        log.info("Buscando productos por término: '{}', página: {}", termino, pageable.getPageNumber());
        if (termino == null || termino.isEmpty()) {
            return Page.empty();
        }
        Page<Products> productsPage = repository.buscarPorCodigoONombre(termino, pageable);
        log.info("Búsqueda por término '{}' encontró {} resultados.", termino, productsPage.getTotalElements());
        return productsPage.map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    @Override
    public Page<ProductsResponseDTO> findByState(String state, Pageable pageable) {
        log.info("Filtrando productos por estado: '{}', página: {}", state, pageable.getPageNumber());
        if (state == null || state.isEmpty() || state.equalsIgnoreCase("ALL")) {
            Page<Products> allProducts = repository.findAll(pageable);
            log.info("Mostrando todos los productos (sin filtro de estado). Total: {}", allProducts.getTotalElements());
            return allProducts.map(this::mapToResponse);
        } else {
            try {
                StatesProducts estadoEnum = StatesProducts.valueOf(state.toUpperCase());
                Page<Products> productsPage = repository.findByState(estadoEnum, pageable);
                log.info("Filtro por estado '{}' encontró {} resultados.", estadoEnum, productsPage.getTotalElements());
                return productsPage.map(this::mapToResponse);
            } catch (IllegalArgumentException e) {
                log.warn("Intento de filtrar por estado inválido: {}", state);
                return Page.empty();
            }
        }
    }

    @Transactional
    @Override
    public ProductsResponseDTO save(ProductRequestDTO request, MultipartFile imageFile, Long id) {
        Products productDB;

        if (id != null && id > 0) {
            productDB = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No se encontró el producto con ID: " + id));
        } else {
            productDB = new Products();
            productDB.setActive(true);
        }

        productDB.setCode(request.getCode());
        productDB.setNameProducto(request.getNameProducto());
        productDB.setDescription(request.getDescription());
        productDB.setMarca(request.getMarca());
        productDB.setPrice(request.getPrice());
        productDB.setStock(request.getStock());
        productDB.setMinStock(request.getMinStock());
        productDB.setState(request.getState());
        productDB.setExpirationDate(request.getExpirationDate());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = guardarArchivo(imageFile);
            productDB.setImage(imagePath);
        }

        Products saveProduct = repository.save(productDB);
        return mapToResponse(saveProduct);
    }

    private String guardarArchivo(MultipartFile file) {
        try {
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path rootPath = Paths.get(uploadDir);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
            Files.copy(file.getInputStream(), rootPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar imagen: " + e.getMessage());
        }
    }

    @Override
    public Optional<ProductsResponseDTO> delete(Long id) {
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
