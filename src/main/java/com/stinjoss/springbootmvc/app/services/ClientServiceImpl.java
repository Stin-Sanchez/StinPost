package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Clients;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ClientRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ClientResponseDTO;
import com.stinjoss.springbootmvc.app.exceptions.BusinessLogicException;
import com.stinjoss.springbootmvc.app.exceptions.DuplicateResourceException;
import com.stinjoss.springbootmvc.app.exceptions.ResourceNotFoundException;
import com.stinjoss.springbootmvc.app.repositories.ClientRepository;
import com.stinjoss.springbootmvc.app.repositories.SalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final SalesRepository salesRepository;

    public ClientServiceImpl(ClientRepository clientRepository, SalesRepository salesRepository) {
        this.clientRepository = clientRepository;
        this.salesRepository = salesRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientResponseDTO> findAll() {
        return clientRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ClientResponseDTO findById(Long id) {
        return clientRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    @Transactional
    @Override
    public ClientResponseDTO save(ClientRequestDTO clientRequest, Long id) {
        validateClientUniqueness(clientRequest, id);

        Clients client;
        if (id != null && id > 0) {
            client = clientRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Cliente no encontrado con ID: " + id));
        } else {
            client = new Clients();
            client.setActive(true);
        }

        client.setName(clientRequest.getName());
        client.setLastname(clientRequest.getLastname());
        client.setEmail(clientRequest.getEmail());
        client.setDni(clientRequest.getDni());
        client.setCellPhone(clientRequest.getCellPhone());
        client.setAge(clientRequest.getAge());
        client.setDirection(clientRequest.getDirection());

        Clients savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar. Cliente no encontrado con ID: " + id));
        
        if (salesRepository.existsByClientId(id)) {
            throw new BusinessLogicException("No se puede eliminar el cliente porque tiene ventas asociadas.");
        }

        client.setActive(false);
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    @Override
    public Long count() {
        return clientRepository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientResponseDTO> findByFullNameOrDni(String termino) {
        if (termino == null || termino.isBlank()) {
            return new ArrayList<>();
        }
        return clientRepository.findBYTerm(termino).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private void validateClientUniqueness(ClientRequestDTO dto, Long currentId) {
        clientRepository.findByEmail(dto.getEmail()).ifPresent(client -> {
            if (currentId == null || !client.getId().equals(currentId)) {
                throw new DuplicateResourceException("El email '" + dto.getEmail() + "' ya está registrado.");
            }
        });
        clientRepository.findByDni(dto.getDni()).ifPresent(client -> {
            if (currentId == null || !client.getId().equals(currentId)) {
                throw new DuplicateResourceException("El DNI '" + dto.getDni() + "' ya está en uso.");
            }
        });
    }

    private ClientResponseDTO mapToResponse(Clients entity) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAge(entity.getAge());
        dto.setDni(entity.getDni());
        dto.setCellPhone(entity.getCellPhone());
        dto.setLastname(entity.getLastname());
        dto.setEmail(entity.getEmail());
        dto.setDirection(entity.getDirection());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setFullName(entity.getNombreCompleto());
        return dto;
    }
}
