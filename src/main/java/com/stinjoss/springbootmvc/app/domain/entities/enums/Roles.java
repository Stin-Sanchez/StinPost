package com.stinjoss.springbootmvc.app.domain.entities.enums;

public enum Roles {
    ADMIN("Administrador"),
    VENDEDOR("Vendedor");

    private final String description;

    Roles(String descripcion) {
        this.description = descripcion;
    }

    public String getDescription() {
        return description;
    }
}
