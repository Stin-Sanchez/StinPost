package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceResponseDTO {
    private Long id;
    private String numberInvoice;
    private LocalDateTime issueDate;
    // Ocultamos el path absoluto por seguridad, o mandamos una URL de descarga
    //Falta implementar
    private String downloadUrl;
}
