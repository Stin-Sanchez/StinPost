package com.stinjoss.springbootmvc.app.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor

public class Products extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "name_product", length = 100, nullable = false)
    private String nameProducto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
    @Column(length = 200, nullable = false)
    private String description;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no debe superar los 50 caracteres")
    @Column(name = "marca", length = 50, nullable = false)
    private String marca;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30, message = "El código no debe superar los 30 caracteres")
    @Column(unique = true, length = 30, nullable = false)
    private String code;

    //@NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatesProducts state;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer minStock;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 255, message = "La ruta de la imagen no debe superar los 255 caracteres")
    private String image;

    @Column(name = "expiration_date", updatable = false)
    private LocalDate expirationDate;

    @OneToMany(mappedBy = "products")
    @JsonIgnore
    private List<SalesDetails> salesDetails;

    // Constructor útil para actualizar stock sin traer todos los datos
    public Products(Long id, Integer stock) {
        this.id = id;
        this.stock = stock;
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
        updateState();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        updateState();
    }

    private void updateState() {
        int currentStock = (this.stock != null) ? this.stock : 0;
        int minimun = (this.stock != null) ? this.minStock : 0;

        if (minimun <= 0) minimun = 5;

        if (currentStock <= 0) {
            //Si el stock es 0 o negativo , forzamos estado AGOTADO
            this.state = StatesProducts.AGOTADO;
            return;
        }

        if (currentStock <= minimun) {
            this.state = StatesProducts.CON_STOCK_MINIMO;
            return;
        }

        int casiAgotado = minimun + 10;

        if (currentStock <= casiAgotado) {
            this.state = StatesProducts.CASI_AGOTADO;
            return;
        }

        if (this.isActive()) {
            this.state = StatesProducts.DISPONIBLE;
        } else {
            this.state = StatesProducts.AGOTADO;
        }

    }

}
