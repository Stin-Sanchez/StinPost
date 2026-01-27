package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Clients, Long> {
    
    List<Clients> findByActiveTrue();

    @Query("SELECT c FROM Clients c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.lastname) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "c.dni LIKE CONCAT('%', :term, '%')")
    List<Clients> findBYTerm(@Param("term") String term);

    Optional<Clients> findByEmail(String email);
    Optional<Clients> findByDni(String dni);
}
