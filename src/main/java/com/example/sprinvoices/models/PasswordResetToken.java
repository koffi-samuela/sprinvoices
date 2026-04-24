package com.example.sprinvoices.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    private LocalDateTime expiresAt;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, UserAccount userAccount) {
        this.token = token;
        this.userAccount = userAccount;
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // valide 30 min
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // getters / setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public UserAccount getUserAccount() { return userAccount; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}