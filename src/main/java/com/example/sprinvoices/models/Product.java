package com.example.sprinvoices.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String category;
}