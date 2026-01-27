package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import com.stinjoss.springbootmvc.app.exceptions.DuplicateResourceException;
import com.stinjoss.springbootmvc.app.exceptions.ResourceNotFoundException;
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
import java.util.UUID;

@Service
public class ProductServiceImp implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImp.class);
    private final ProductRepository repository;

    @Value("${upload.dir}")
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
    public ProductsResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
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

    @Transactional
    @Override
    public ProductsResponseDTO save(ProductRequestDTO request, MultipartFile imageFile, Long id) {
        repository.findByCode(request.getCode()).ifPresent(product -> {
            if (id == null || !product.getId().equals(id)) {
                throw new DuplicateResourceException("El código de producto '" + request.getCode() + "' ya existe.");
            }
        });

        Products product;
        if (id != null && id > 0) {
            product = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Producto no encontrado con ID: " + id));
        } else {
            product = new Products();
            product.setActive(true);
        }

        product.setCode(request.getCode());
        product.setNameProducto(request.getNameProducto());
        product.setDescription(request.getDescription());
        product.setMarca(request.getMarca());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setMinStock(request.getMinStock());
        product.setState(request.getState());
        product.setExpirationDate(request.getExpirationDate());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = guardarArchivo(imageFile);
            product.setImage(imagePath);
        }

        Products savedProduct = repository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Products product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar. Producto no encontrado con ID: " + id));

        product.setActive(false);
        product.setState(StatesProducts.AGOTADO);
        repository.save(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Long count() {
        return repository.count();
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
