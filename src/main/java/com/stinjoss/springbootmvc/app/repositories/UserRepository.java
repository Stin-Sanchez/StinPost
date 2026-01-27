package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.username LIKE %:term% OR u.dni LIKE %:term%")
    List<User> findByUsernameORDni(@Param("term") String term);

    // Este es el método que usaremos para el login
    Optional<User> findByUsername(String username);
    
    // Estos son para validaciones de duplicados
    Optional<User> findByEmail(String email);
    Optional<User> findByDni(String dni);
}
