package com.stinjoss.springbootmvc.app.domain.entities.responseDTOS;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private String username;
    private String role; // Si manejas roles
    private LocalDateTime createdAt;
    // JAMÁS devuelvas el password aquí
}