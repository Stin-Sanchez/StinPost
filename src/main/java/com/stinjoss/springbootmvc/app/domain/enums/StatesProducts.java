package com.stinjoss.springbootmvc.app.domain.enums;

public enum StatesProducts {
    DISPONIBLE("Disponible"),
    AGOTADO("Agotado"),
    CASI_AGOTADO("Casi Agotado"),
    CON_STOCK_MINIMO("Con Stock Mínimo");
    private final String description;

    StatesProducts(String descripcion) {
        this.description = descripcion;
    }

    public String getDescription() {
        return description;
    }
}
