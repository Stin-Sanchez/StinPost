package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import com.stinjoss.springbootmvc.app.domain.entities.enums.PaymentMethods;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class SalesResponseDTO {
    private Long id;
    private BigDecimal total;
    private StatesSales estado;
    private PaymentMethods paymentMethods;
    private LocalDateTime createdAt;

    // Objetos anidados planos (Responses)
    private ClientResponseDTO client;
    private UserResponseDTO seller;
    private InvoiceResponseDTO invoice;

    @NotEmpty(message = "La venta debe tener productos")
    private List<SalesDetailsResponseDTO> details;
}