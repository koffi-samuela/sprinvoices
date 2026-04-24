package com.example.sprinvoices.repository;

import com.example.sprinvoices.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserAccountId(Long userAccountId); // nettoie les anciens tokens
}