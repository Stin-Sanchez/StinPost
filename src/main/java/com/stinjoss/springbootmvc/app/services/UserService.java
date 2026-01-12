package com.stinjoss.springbootmvc.app.services;


import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.UserRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;

import java.util.List;
import java.util.Optional;


public interface UserService {

    List<UserResponseDTO> findAll();

    Optional<UserResponseDTO> findById(Long id);

    UserResponseDTO save(UserRequestDTO user, Long id);

    Optional<UserResponseDTO> delete(Long id);

    List<UserResponseDTO> findByUsernameOrDni(String termino);

    Optional<UserResponseDTO> findByUsername(String termino);

    UserResponseDTO login(String username, String password);

    Long count();
}
