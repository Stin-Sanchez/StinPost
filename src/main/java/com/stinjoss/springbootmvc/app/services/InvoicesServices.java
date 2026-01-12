package com.stinjoss.springbootmvc.app.services;

import com.stinjoss.springbootmvc.app.domain.entities.Invoices;

import java.util.List;
import java.util.Optional;

public interface InvoicesServices {

    List<Invoices> findAll();

    Optional<Invoices> findById(Long id);

    Invoices save(Invoices client);

    Optional<Invoices> delete(Long id);

    Long count();

    List<Invoices> findByNumberFac(String termino);
}
