package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Long> {

    Page<Products> findByActiveTrue(Pageable pageable);

    Page<Products> findByState(StatesProducts state, Pageable pageable);

    @Query("SELECT p FROM Products p WHERE p.code LIKE %:term% OR p.nameProducto LIKE %:term%")
    Page<Products> buscarPorCodigoONombre(@Param("term") String term, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Products p WHERE p.stock <= p.minStock AND p.active = true")
    Long countLowStockProducts();

    Optional<Products> findByCode(String code);
}
