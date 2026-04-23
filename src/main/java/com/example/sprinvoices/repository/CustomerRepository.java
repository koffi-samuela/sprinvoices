package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByUserAccountId(Long userId);
    Customer findByUserAccountUsername(String username);
}