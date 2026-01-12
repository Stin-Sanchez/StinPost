package com.stinjoss.springbootmvc.app.services;


import com.stinjoss.springbootmvc.app.domain.entities.Invoices;
import com.stinjoss.springbootmvc.app.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceServiceImp implements InvoicesServices {

    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImp(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoices> findAll() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Invoices> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional
    @Override
    public Invoices save(Invoices client) {
        return invoiceRepository.save(client);
    }

    @Transactional
    @Override
    public Optional<Invoices> delete(Long id) {
        Optional<Invoices> invoicesOptional = invoiceRepository.findById(id);
        if (invoicesOptional.isPresent()) {
            invoiceRepository.deleteById(id);
            return invoicesOptional;
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public Long count() {
        return invoiceRepository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Invoices> findByNumberFac(String termino) {
        if (termino == null || termino.isEmpty()) {
            return new ArrayList<>();
        }
        return invoiceRepository.findByTerm(termino);
    }
}
