package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.Invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerId(Long customerId);
    // Filtre par état et/ou client
   @Query("""
        SELECT i FROM Invoice i
        WHERE (:customerId IS NULL OR i.customer.id = :customerId)
        AND (:status IS NULL OR
            (:status = 'CREATED' AND i.invoicedAt IS NULL) OR
            (:status = 'INVOICED' AND i.invoicedAt IS NOT NULL AND i.paidAt IS NULL) OR
            (:status = 'PAID' AND i.paidAt IS NOT NULL))
    """)
    Page<Invoice> findWithFilters(
            @Param("customerId") Long customerId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.createdAt) = :year")
    long countByYear(@Param("year") int year);
}