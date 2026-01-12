package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Sales;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {

    List<Sales> findByState(StatesSales state);

    @Query("SELECT s FROM Sales s WHERE " +
            "LOWER(s.client.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(s.client.lastname) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "s.invoice.numberInvoice LIKE %:term%")
    List<Sales> findByClientOrInvoice(@Param("term") String term);
}
