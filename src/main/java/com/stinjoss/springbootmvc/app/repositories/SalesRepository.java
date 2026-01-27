package com.stinjoss.springbootmvc.app.repositories;

import com.stinjoss.springbootmvc.app.domain.entities.Sales;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {

    List<Sales> findByState(StatesSales state);

    @Query("SELECT s FROM Sales s WHERE " +
            "LOWER(s.client.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(s.client.lastname) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "s.invoice.numberInvoice LIKE %:term%")
    List<Sales> findByClientOrInvoice(@Param("term") String term);

    // Métodos para Dashboard
    @Query("SELECT SUM(s.total) FROM Sales s WHERE s.createdAt BETWEEN :start AND :end AND s.state = 'FACTURADA'")
    BigDecimal sumTotalSalesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sales s WHERE s.createdAt BETWEEN :start AND :end AND s.state = 'FACTURADA'")
    Long countSalesByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Últimas 5 ventas
    List<Sales> findTop5ByOrderByCreatedAtDesc();

    // Gráfico: Ventas por día (Devuelve Object[]: [Fecha, Total])
    // Usamos FUNCTION('DATE', ...) para extraer solo la parte de la fecha en H2/MySQL
    @Query("SELECT FUNCTION('DATE', s.createdAt) as fecha, SUM(s.total) as total " +
            "FROM Sales s " +
            "WHERE s.createdAt >= :startDate AND s.state = 'FACTURADA' " +
            "GROUP BY FUNCTION('DATE', s.createdAt) " +
            "ORDER BY fecha ASC")
    List<Object[]> findDailySalesSum(@Param("startDate") LocalDateTime startDate);
}
