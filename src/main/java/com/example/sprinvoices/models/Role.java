package com.example.sprinvoices.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data                // Lombok génère automatiquement : getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Lombok génère un constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok génère un constructeur avec tous les champs
@Entity
@Table(name = "ROLE")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}