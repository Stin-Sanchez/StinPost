package com.stinjoss.springbootmvc.app.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@NoArgsConstructor
@Getter
@Setter

public class Invoices extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "No_invoice", nullable = false, unique = true)
    private String numberInvoice;

    @NotBlank(message = "Debe especificar donde guardar la factura ")
    @Column(name = "path_invoice", nullable = false)
    private String pathInvoice;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.issueDate = LocalDateTime.now();
    }
}
