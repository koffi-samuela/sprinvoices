package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.QuoteRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRowRepository extends JpaRepository<QuoteRow, Long> {
}