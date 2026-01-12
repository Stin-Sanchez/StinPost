package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String lastname;
    private String dni;
    private String email;
    private String cellPhone;
    private Byte age;
    private String direction;
    private boolean active;
    private LocalDateTime createdAt;

    // Agregamos un campo de utilidad
    private String fullName;
}