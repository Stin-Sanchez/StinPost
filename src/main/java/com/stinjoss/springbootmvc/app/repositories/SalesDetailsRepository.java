package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.SalesDetails;
import com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.TopProductDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesDetailsRepository extends JpaRepository<SalesDetails, Long> {

    @Query("SELECT new com.stinjoss.springbootmvc.app.domain.entities.responseDTOS.TopProductDTO(d.products.nameProducto, SUM(d.quantity)) " +
            "FROM SalesDetails d " +
            "GROUP BY d.products.nameProducto " +
            "ORDER BY SUM(d.quantity) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);
}
