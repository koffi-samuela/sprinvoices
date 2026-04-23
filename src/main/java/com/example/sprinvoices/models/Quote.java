package com.example.sprinvoices.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "QUOTE")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_number", length = 20, unique = true)
    private String number;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ToString.Exclude
    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteRow> rows = new ArrayList<>();

    public double total() {
        return rows.stream().mapToDouble(QuoteRow::amount).sum();
    }

    public String getStatusLabel() {
        return switch (status) {
            case "DRAFT"    -> "BROUILLON";
            case "SENT"     -> "ENVOYÉ";
            case "ACCEPTED" -> "ACCEPTÉ";
            case "REFUSED"  -> "REFUSÉ";
            default         -> status;
        };
    }
}