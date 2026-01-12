package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductsResponseDTO {
    private Long id;
    private String nameProducto;
    private String description;
    private String marca;
    private String code;
    private StatesProducts state;
    private Integer stock;
    private Integer minStock;
    private BigDecimal price;
    private String image;
    private LocalDate expirationDate;
}