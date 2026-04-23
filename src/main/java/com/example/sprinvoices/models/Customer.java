package com.example.sprinvoices.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "corporate_name", length = 150)
    private String corporateName;

    @Column(length = 255)
    private String address;

    @Column(length = 10)
    private String zipcode;

    @Column(length = 100)
    private String city;

    @Column(nullable = false)
    private int delay = 30;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private UserAccount userAccount;

    @ToString.Exclude
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private java.util.List<Invoice> invoices = new java.util.ArrayList<>();
}