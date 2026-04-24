package com.example.sprinvoices.controller;

import com.example.sprinvoices.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/client/dashboard";
    }

    // ── Étape 1 : afficher le formulaire "mot de passe oublié" ──
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // ── Étape 1 : traiter la soumission de l'email ──
    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            HttpServletRequest request,
            Model model) {

        // Reconstruit l'URL de base dynamiquement (http://localhost:8080 ou prod)
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();

        passwordResetService.initiateReset(email, baseUrl);

        // Toujours afficher le même message (sécurité : ne pas révéler si l'email existe)
        model.addAttribute("message",
                "Si cet email est associé à un compte, un lien vous a été envoyé.");
        return "forgot-password";
    }

    // ── Étape 2 : afficher le formulaire de nouveau mot de passe ──
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        if (!passwordResetService.isTokenValid(token)) {
            model.addAttribute("error", "Ce lien est invalide ou a expiré.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    // ── Étape 2 : traiter le nouveau mot de passe ──
    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Les mots de passe ne correspondent pas.");
            return "reset-password";
        }

        if (password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Le mot de passe doit contenir au moins 8 caractères.");
            return "reset-password";
        }

        boolean success = passwordResetService.resetPassword(token, password);
        if (!success) {
            model.addAttribute("error", "Ce lien est invalide ou a expiré.");
            return "reset-password";
        }

        return "redirect:/login?resetSuccess=true";
    }
}