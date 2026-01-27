package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.User;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.UserRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.exceptions.BusinessLogicException;
import com.stinjoss.springbootmvc.app.exceptions.DuplicateResourceException;
import com.stinjoss.springbootmvc.app.exceptions.ResourceNotFoundException;
import com.stinjoss.springbootmvc.app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDTO> findAll() {
        return repository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    @Override
    public UserResponseDTO save(UserRequestDTO userRequest, Long id) {
        validateUserUniqueness(userRequest, id);

        User user;
        if (id != null && id > 0) {
            user = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Usuario no encontrado con ID: " + id));
        } else {
            user = new User();
            user.setActive(true);
            if (userRequest.getPassword() == null || userRequest.getPassword().isBlank()) {
                throw new BusinessLogicException("La contraseña es obligatoria al crear un nuevo usuario.");
            }
        }

        user.setName(userRequest.getName());
        user.setLastname(userRequest.getLastname());
        user.setEmail(userRequest.getEmail());
        user.setDni(userRequest.getDni());
        user.setCellPhone(userRequest.getCellPhone());
        user.setAge(userRequest.getAge());
        user.setUsername(userRequest.getUsername());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            validatePasswordStrength(userRequest.getPassword());
            user.setPassword(userRequest.getPassword());
        }

        User savedUser = repository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar. Usuario no encontrado con ID: " + id));
        
        if ("admin".equalsIgnoreCase(user.getUsername()) || user.getId() == 1L) {
            throw new BusinessLogicException("No se puede eliminar al usuario administrador principal.");
        }

        user.setActive(false);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDTO> findByUsernameOrDni(String termino) {
        return repository.findByUsernameORDni(termino).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserResponseDTO> findByUsername(String termino) {
        return repository.findByUsername(termino).map(this::mapToResponse);
    }

    @Transactional(readOnly = true) // El login principal es de solo lectura
    @Override
    public UserResponseDTO login(String username, String password) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new BusinessLogicException("Usuario o contraseña incorrectos."));

        if (!user.isActive()) {
            throw new BusinessLogicException("La cuenta de usuario está inactiva.");
        }

        if (!user.getPassword().equals(password)) {
            throw new BusinessLogicException("Usuario o contraseña incorrectos.");
        }
        
        // La actualización se delega a un método con su propia transacción
        updateLastAccess(user.getId());
        
        return mapToResponse(user);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastAccess(Long userId) {
        try {
            repository.findById(userId).ifPresent(user -> {
                user.setLastAccess(LocalDateTime.now());
                repository.save(user);
                log.info("Fecha de último acceso actualizada para el usuario {}", userId);
            });
        } catch (Exception e) {
            log.error("ADVERTENCIA: No se pudo actualizar la fecha de último acceso para el usuario {}. Causa: {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Long count() {
        return repository.count();
    }

    private void validateUserUniqueness(UserRequestDTO dto, Long currentId) {
        repository.findByUsername(dto.getUsername()).ifPresent(user -> {
            if (currentId == null || !user.getId().equals(currentId)) {
                throw new DuplicateResourceException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso.");
            }
        });
        repository.findByEmail(dto.getEmail()).ifPresent(user -> {
            if (currentId == null || !user.getId().equals(currentId)) {
                throw new DuplicateResourceException("El email '" + dto.getEmail() + "' ya está registrado.");
            }
        });
        repository.findByDni(dto.getDni()).ifPresent(user -> {
            if (currentId == null || !user.getId().equals(currentId)) {
                throw new DuplicateResourceException("El DNI '" + dto.getDni() + "' ya está en uso.");
            }
        });
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BusinessLogicException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    private UserResponseDTO mapToResponse(User entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLastname(entity.getLastname());
        dto.setEmail(entity.getEmail());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUsername(entity.getUsername());
        return dto;
    }
}
