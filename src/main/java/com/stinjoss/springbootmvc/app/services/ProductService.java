package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<ProductsResponseDTO> findAll();

    Optional<ProductsResponseDTO> findById(Long id);

    ProductsResponseDTO save(ProductRequestDTO product, MultipartFile imageFile, Long id);

    List<ProductsResponseDTO> buscarPorTermino(String termino);

    List<ProductsResponseDTO> findByState(String state);

    Optional<ProductsResponseDTO> delete(Long id);

    Long count();
}
