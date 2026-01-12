package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Clients;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.ClientRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.ClientResponseDTO;
import com.stinjoss.springbootmvc.app.repositories.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;

    public ClientServiceImpl(ClientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientResponseDTO> findAll() {
        return repository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ClientResponseDTO> findById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    @Override
    public ClientResponseDTO save(ClientRequestDTO client, Long id) {
        Clients clientDB;

        //Verificamos si es una edición
        if (id != null && id > 0) {
            //Buscamos al usuario a actualizar
            clientDB = repository.findById(id).
                    orElseThrow(() -> new RuntimeException("No se encontró el cliente con ID: " + id));
        } else {
            clientDB = new Clients();
            clientDB.setActive(true);

        }
        //Actualizamos todos los datos básicos
        clientDB.setName(client.getName());
        clientDB.setLastname(client.getLastname());
        clientDB.setEmail(client.getEmail());
        clientDB.setDni(client.getDni());
        clientDB.setCellPhone(client.getCellPhone());
        clientDB.setAge(client.getAge());
        clientDB.setDirection(client.getDirection());

        Clients clientSaved = repository.save(clientDB);

        return mapToResponse(clientSaved);
    }

    @Transactional
    @Override
    public Optional<ClientResponseDTO> delete(Long id) {
        Optional<Clients> clientsOptional = repository.findById(id);
        if (clientsOptional.isPresent()) {
            Clients client = clientsOptional.get();
            client.setActive(false);
            return clientsOptional.map(this::mapToResponse);
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public Long count() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClientResponseDTO> findByFullNameOrDni(String termino) {

        if (termino == null || termino.isEmpty()) {
            return new ArrayList<>();
        }

        //return repository.findByNameContainingIgnoreCase(termino);
        return repository.findBYTerm(termino).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    //Mapper
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
