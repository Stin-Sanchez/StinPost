package com.stinjoss.springbootmvc.app.domain.enums;

public enum PaymentMethods {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia ");

    private final String description;

    PaymentMethods(String description) {
        this.description = description;
    }

    public String getDescripcion() {
        return description;
    }
}
