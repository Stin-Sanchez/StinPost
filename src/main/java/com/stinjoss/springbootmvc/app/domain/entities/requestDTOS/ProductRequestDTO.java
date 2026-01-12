package com.stinjoss.springbootmvc.app.domain.entities.requestDTOS;

import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    private String nameProducto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200)
    private String description;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50)
    private String marca;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30)
    private String code;

    //@NotNull(message = "El estado es obligatorio")
    private StatesProducts state;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0)
    private Integer stock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0)
    private Integer minStock;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    private LocalDate expirationDate;
}
