package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.User;
import com.stinjoss.springbootmvc.app.domain.entities.requestDTOS.UserRequestDTO;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.UserResponseDTO;
import com.stinjoss.springbootmvc.app.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDTO> findAll() {
        return this.repository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserResponseDTO> findById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDTO> findByUsernameOrDni(String termino) {
        if (termino == null || termino.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findByUsernameORDni(termino).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserResponseDTO> findByUsername(String termino) {
        if (termino == null || termino.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(repository.findByUsername(termino)).map(this::mapToResponse);
    }

    @Transactional
    @Override
    public UserResponseDTO save(UserRequestDTO user, Long id) {
        User userDb;

        //Verificamos si es una edición
        if (id != null && id > 0) {
            //Buscamos al usuario a actualizar
            userDb = repository.findById(id).
                    orElseThrow(() -> new RuntimeException("No se encontró el producto con ID: " + id));

        } else {
            userDb = new User();
            userDb.setActive(true);
        }

        //Actualizamos todos los datos básicos
        userDb.setName(user.getName());
        userDb.setLastname(user.getLastname());
        userDb.setEmail(user.getEmail());
        userDb.setDni(user.getDni());
        userDb.setCellPhone(user.getCellPhone());
        userDb.setAge(user.getAge());
        userDb.setUsername(user.getUsername());

        // PROTECCIÓN DE CONTRASEÑA
        // Solo actualizamos si el usuario escribió una nueva contraseña
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            userDb.setPassword(user.getPassword());
        }

        User userSaved = repository.save(userDb);
        return mapToResponse(userSaved);

    }

    @Transactional
    @Override
    public Optional<UserResponseDTO> delete(Long id) {
        Optional<User> userOptional = repository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(false);
            repository.save(user);
            return userOptional.map(this::mapToResponse);
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public UserResponseDTO login(String username, String password) {
        // Buscamos la ENTIDAD
        User userDB = Optional.ofNullable(repository.findByUsername(username)).orElseThrow();
        //Validamos si existe y si la contraseña coincide
        if (userDB != null && userDB.getPassword().equals(password)) {

            // Convertimos la entidad a DTO para devolverla al controlador
            return mapToResponse(userDB);
        }

        // 3. Si falla algo, retornamos null

        return null;
    }

    @Transactional
    @Override
    public Long count() {
        return repository.count();
    }

    //Mapper
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
