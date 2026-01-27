package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.SalesRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.DashboardStatsDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.SalesResponseDTO;

import java.util.List;

public interface SalesService {

    List<SalesResponseDTO> findAll();

    SalesResponseDTO findById(Long id);

    SalesResponseDTO save(SalesRequestDTO sale, Long idVendedor);

    void anularVenta(Long id);

    List<SalesResponseDTO> findByState(String state);
    
    List<SalesResponseDTO> findByTerm(String term);

    DashboardStatsDTO getDashboardStats();
}
