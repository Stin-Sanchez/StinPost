package com.stinjoss.springbootmvc.app.domain.entities.requestDTOS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequestDTO {
    // Datos de Person
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50)
    private String lastname;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10)
    private String dni;

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String cellPhone;

    private Byte age;

    // Datos propios de Client
    private String direction;
}
