package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ClientRepository extends JpaRepository<Clients, Long> {
    // SQL que genera: SELECT * FROM clients WHERE UPPER(name) LIKE UPPER('%termino%')
    List<Clients> findByNameContainingIgnoreCase(String name);

    List<Clients> findByActiveTrue();

    @Query("SELECT c FROM Clients c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.lastname) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "c.dni LIKE CONCAT('%', :term, '%')")
    List<Clients> findBYTerm(@Param("term") String term);
}
