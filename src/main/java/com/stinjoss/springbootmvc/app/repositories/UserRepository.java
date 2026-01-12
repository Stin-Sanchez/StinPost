package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.username LIKE %:term% OR u.dni LIKE %:term%")
    List<User> findByUsernameORDni(@Param("term") String term);

    User findByUsername(String username);
}
