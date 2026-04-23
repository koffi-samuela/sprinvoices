package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}