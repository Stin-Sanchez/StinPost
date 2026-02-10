package com.stinjoss.springbootmvc.app.domain.dtos.requestDTOS;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequestDTO {

    @NotBlank(message = "El nombre del rol es obligatorio")
    String name;

}
