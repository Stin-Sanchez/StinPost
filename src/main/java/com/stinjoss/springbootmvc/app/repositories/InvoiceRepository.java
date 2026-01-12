package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface InvoiceRepository extends JpaRepository<Invoices, Long> {

    @Query("SELECT i FROM Invoices i WHERE i.numberInvoice LIKE %:term%")
    List<Invoices> findByTerm(@Param("term") String term);

    Invoices findTopByOrderByIdDesc();
}
