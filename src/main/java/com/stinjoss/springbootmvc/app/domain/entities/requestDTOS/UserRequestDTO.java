package com.stinjoss.springbootmvc.app.domain.entities.requestDTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {
    // Datos Personales
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastname;

    @NotBlank(message = "La cédula es obligatoria")
    private String dni;

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String cellPhone;

    private Byte age;

    // Credenciales
    @NotEmpty(message = "El usuario es obligatorio")
    @Size(min = 4, max = 16)
    private String username;

    @NotEmpty(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 16)
    private String password;
}
