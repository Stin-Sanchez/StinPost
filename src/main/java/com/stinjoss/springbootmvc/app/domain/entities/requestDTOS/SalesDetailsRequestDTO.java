package com.stinjoss.springbootmvc.app.domain.entities.requestDTOS;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalesDetailsRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int quantity;

    // El precio NO se envía, se busca en la BD por seguridad.
}