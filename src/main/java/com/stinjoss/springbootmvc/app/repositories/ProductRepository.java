package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Products, Long> {

    List<Products> findByActiveTrue();

    List<Products> findByState(StatesProducts state);

    @Query("SELECT p FROM Products p WHERE p.code LIKE %:term% OR p.nameProducto LIKE %:term%")
    List<Products> buscarPorCodigoONombre(@Param("term") String term);
}
