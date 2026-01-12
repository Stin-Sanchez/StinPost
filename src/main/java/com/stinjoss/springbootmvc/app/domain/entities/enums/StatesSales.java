package com.stinjoss.springbootmvc.app.domain.entities.enums;

public enum StatesSales {
    //Estados de ventas
    FACTURADA("Facturada"),
    ANULADA("Anulada"),
    ELIMINADA("Eliminada");

    private final String description;

    StatesSales(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
