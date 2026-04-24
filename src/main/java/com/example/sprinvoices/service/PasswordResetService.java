package com.example.sprinvoices.service;

import com.example.sprinvoices.models.PasswordResetToken;
import com.example.sprinvoices.models.UserAccount;
import com.example.sprinvoices.repository.PasswordResetTokenRepository;
import com.example.sprinvoices.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Étape 1 — L'utilisateur soumet son email.
     * On génère un token et on envoie le lien par email.
     * On ne révèle pas si l'email existe ou non (sécurité).
     */
    @Transactional
    public void initiateReset(String email, String baseUrl) {
        Optional<UserAccount> opt = userAccountRepository.findByUsername(email);
        if (opt.isEmpty()) return; // silencieux : on n'informe pas que l'email est inconnu

        UserAccount user = opt.get();

        // Supprime un éventuel token précédent pour cet utilisateur
        tokenRepository.deleteByUserAccountId(user.getId());
        tokenRepository.flush();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    /**
     * Étape 2 — Validation du token (affichage du formulaire).
     * Retourne true si le token est valide et non expiré.
     */
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired())
                .orElse(false);
    }

    /**
     * Étape 3 — Enregistrement du nouveau mot de passe.
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty() || opt.get().isExpired()) return false;

        PasswordResetToken resetToken = opt.get();
        UserAccount user = resetToken.getUserAccount();

        user.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        // Supprime le token après usage (usage unique)
        tokenRepository.delete(resetToken);

        return true;
    }
}