package com.stinjoss.springbootmvc.app.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stinjoss.springbootmvc.app.domain.entities.enums.PaymentMethods;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesSales;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sales extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private StatesSales state; // Enum recomendado


    @NotNull(message = "El método de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_methods", nullable = false, length = 20)
    private PaymentMethods paymentMethods;

    @Positive(message = "El total de la venta debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "La venta debe estar asociada a un cliente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Clients client;

    @NotNull(message = "La venta debe estar asociada a un vendedor")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;


    // @NotEmpty(message = "La venta debe tener al menos un detalle")
    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SalesDetails> details = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", unique = true)
    private Invoices invoice;

}
