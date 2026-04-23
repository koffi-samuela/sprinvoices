package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.InvoiceRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRowRepository extends JpaRepository<InvoiceRow, Long> {
}