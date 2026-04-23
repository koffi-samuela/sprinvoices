package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByCustomerId(Long customerId);
}