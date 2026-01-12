package com.stinjoss.springbootmvc.app.domain.entities.requestDTOS;

import com.stinjoss.springbootmvc.app.domain.entities.enums.PaymentMethods;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesRequestDTO {

    // Solo enviamos el ID, no el objeto entero
    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethods paymentMethods;

    // Lista de items a comprar
    @NotEmpty(message = "La venta debe tener productos")
    private List<SalesDetailsRequestDTO> details;

    // NOTA: No enviamos 'sellerId' (se toma del login)
    // NOTA: No enviamos 'total' (se calcula en backend)
    // NOTA: No enviamos 'invoice' (se genera automática)
}