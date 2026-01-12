package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.SalesResponseDTO;

import java.util.List;
import java.util.Optional;

public interface SalesService {

    List<SalesResponseDTO> findAll();

    Optional<SalesResponseDTO> findById(Long id);

    SalesResponseDTO save(SalesRequestDTO sale, Long idVendedor);

    Optional<SalesResponseDTO> delete(Long id);

    Optional<SalesResponseDTO> anularVenta(Long id);

    List<SalesResponseDTO> findByState(String state);

    List<SalesResponseDTO> findByTerm(String term);

    Long count();
}
