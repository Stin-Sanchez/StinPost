package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ProductRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ProductsResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    Page<ProductsResponseDTO> findAll(Pageable pageable);

    ProductsResponseDTO findById(Long id);

    ProductsResponseDTO save(ProductRequestDTO product, MultipartFile imageFile, Long id);

    Page<ProductsResponseDTO> buscarPorTermino(String termino, Pageable pageable);

    Page<ProductsResponseDTO> findByState(String state, Pageable pageable);

    void delete(Long id);

    Long count();
}
