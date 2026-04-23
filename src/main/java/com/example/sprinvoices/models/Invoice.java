package com.example.sprinvoices.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INVOICE")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "invoiced_at")
    private LocalDateTime invoicedAt;   // null = état CREATED

    @Column(name = "paid_at")
    private LocalDateTime paidAt;  
    
    @Column(name = "fac_number", length = 20, unique = true)
    private String number;// null = pas encore payée

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ToString.Exclude
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceRow> rows = new ArrayList<>();

    // Calcule le total de la facture en additionnant toutes les lignes
    public double total() {
        return rows.stream()
                   .mapToDouble(InvoiceRow::amount)
                   .sum();
    }

    // Retourne l'état lisible de la facture
    public String getStatus() {
        if (paidAt != null)     return "PAYÉE";
        if (invoicedAt != null) return "FACTURÉE";
        return "CRÉÉE";
    }
}