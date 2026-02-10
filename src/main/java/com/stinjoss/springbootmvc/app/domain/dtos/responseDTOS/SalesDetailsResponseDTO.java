package com.stinjoss.springbootmvc.app.domain.dtos.responseDTOS;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesDetailsResponseDTO {
    private Long productId;
    private String productName;// Muy útil para la tabla del front
    private int quantity;
    private BigDecimal priceProduct;

}
