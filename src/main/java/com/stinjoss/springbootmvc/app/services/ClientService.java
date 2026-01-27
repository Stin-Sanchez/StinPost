package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ClientRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ClientResponseDTO;

import java.util.List;

public interface ClientService {

    List<ClientResponseDTO> findAll();

    ClientResponseDTO findById(Long id);

    ClientResponseDTO save(ClientRequestDTO client, Long id);

    void delete(Long id);
    
    Long count();

    List<ClientResponseDTO> findByFullNameOrDni(String termino);
}
