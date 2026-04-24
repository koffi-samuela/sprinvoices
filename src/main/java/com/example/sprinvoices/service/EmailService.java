package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.Quote;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
private PdfService pdfService;

    // Envoi asynchrone pour ne pas bloquer la requête HTTP
@Async
public void sendInvoicedNotification(Invoice invoice) {
    String email = invoice.getCustomer().getUserAccount().getUsername();
    String subject = "Votre facture " + invoice.getNumber() + " est disponible";
    String body = buildInvoicedEmail(invoice);
    sendHtmlEmailWithPdf(email, subject, body, invoice);
}

@Async
public void sendPaidConfirmation(Invoice invoice) {
    String email = invoice.getCustomer().getUserAccount().getUsername();
    String subject = "Paiement confirmé — " + invoice.getNumber();
    String body = buildPaidEmail(invoice);
    sendHtmlEmailWithPdf(email, subject, body, invoice);
}

private void sendHtmlEmailWithPdf(String to, String subject, String htmlBody, Invoice invoice) {
    try {
        byte[] pdfBytes = pdfService.generatePdfBytes(invoice);

        MimeMessage message = mailSender.createMimeMessage();
        // true = multipart
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        // Pièce jointe PDF
        helper.addAttachment(
            invoice.getNumber() + ".pdf",
            new org.springframework.core.io.ByteArrayResource(pdfBytes)
        );

        mailSender.send(message);
    } catch (Exception e) {
        System.err.println("Erreur envoi email avec PDF : " + e.getMessage());
    }
}

private String buildInvoicedEmail(Invoice invoice) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#ffffff;border:1px solid #eee;border-radius:8px;overflow:hidden;">

            <div style="background:#C0392B;padding:24px;">
                <h1 style="color:white;margin:0;font-size:20px;">SprinVoices</h1>
                <p style="color:#f5f5f5;margin:4px 0 0;font-size:13px;">Facturation & gestion financière</p>
            </div>

            <div style="padding:32px;">
                <h2 style="color:#2c3e50;margin-top:0;">Votre facture est disponible</h2>

                <p style="color:#555;font-size:14px;">
                    Bonjour <strong>%s</strong>,
                </p>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Votre facture <strong>%s</strong> d’un montant de
                    <strong>%.2f € HT</strong> est désormais disponible dans votre espace client.
                </p>

                <div style="background:#f8f9fa;padding:16px;border-radius:6px;margin:24px 0;">
                    <p style="margin:0;color:#555;font-size:14px;">
                        Délai de paiement : <strong>%d jours</strong>
                    </p>
                </div>

                <p style="color:#777;font-size:13px;">
                    Vous pouvez consulter et télécharger votre facture depuis votre espace client.
                </p>
            </div>

            <div style="background:#f8f9fa;padding:14px;text-align:center;">
                <p style="color:#999;font-size:12px;margin:0;">
                    SprinVoices — Tous droits réservés
                </p>
            </div>
        </div>
        """.formatted(
            invoice.getCustomer().getName(),
            invoice.getNumber(),
            invoice.total(),
            invoice.getCustomer().getDelay()
        );
}

private String buildPaidEmail(Invoice invoice) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#ffffff;border:1px solid #eee;border-radius:8px;overflow:hidden;">

            <div style="background:#27ae60;padding:24px;">
                <h1 style="color:white;margin:0;font-size:20px;">SprinVoices</h1>
                <p style="color:#eafaf1;margin:4px 0 0;font-size:13px;">Confirmation de paiement</p>
            </div>

            <div style="padding:32px;">
                <h2 style="color:#2c3e50;margin-top:0;">Paiement confirmé</h2>

                <p style="color:#555;font-size:14px;">
                    Bonjour <strong>%s</strong>,
                </p>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Nous confirmons la réception du paiement de votre facture
                    <strong>%s</strong> d’un montant de <strong>%.2f € HT</strong>.
                </p>

                <p style="color:#555;font-size:14px;">
                    Merci pour votre règlement.
                </p>
            </div>

            <div style="background:#f8f9fa;padding:14px;text-align:center;">
                <p style="color:#999;font-size:12px;margin:0;">
                    SprinVoices — Tous droits réservés
                </p>
            </div>
        </div>
        """.formatted(
            invoice.getCustomer().getName(),
            invoice.getNumber(),
            invoice.total()
        );
}
    
    // ── Devis ────────────────────────────────────────────────────
@Async
public void sendQuoteSentNotification(com.example.sprinvoices.models.Quote quote) {
    String email = quote.getCustomer().getUserAccount().getUsername();
    String subject = "Votre devis " + quote.getNumber() + " est disponible";
    String body = buildQuoteSentEmail(quote);
    sendHtmlEmail(email, subject, body);
}

@Async
public void sendQuoteConvertedNotification(com.example.sprinvoices.models.Quote quote) {
    String email = quote.getCustomer().getUserAccount().getUsername();
    String subject = "Votre devis " + quote.getNumber() + " a été converti en facture";
    String body = buildQuoteConvertedEmail(quote);
    sendHtmlEmail(email, subject, body);
}

// Méthode email simple sans pièce jointe (pour les devis)
private void sendHtmlEmail(String to, String subject, String htmlBody) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    } catch (Exception e) {
        System.err.println("Erreur envoi email : " + e.getMessage());
    }
}

private String buildQuoteSentEmail(Quote quote) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#ffffff;border:1px solid #eee;border-radius:8px;overflow:hidden;">

            <div style="background:#8e44ad;padding:24px;">
                <h1 style="color:white;margin:0;font-size:20px;">SprinVoices</h1>
                <p style="color:#f3eaff;margin:4px 0 0;font-size:13px;">Proposition commerciale</p>
            </div>

            <div style="padding:32px;">
                <h2 style="color:#2c3e50;margin-top:0;">Votre devis est disponible</h2>

                <p style="color:#555;font-size:14px;">
                    Bonjour <strong>%s</strong>,
                </p>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Votre devis <strong>%s</strong> d’un montant de
                    <strong>%.2f € HT</strong> est disponible dans votre espace client.
                </p>

                <p style="color:#777;font-size:13px;margin-top:16px;">
                    Vous pouvez le consulter, l’accepter ou le refuser directement depuis votre espace.
                </p>
            </div>

            <div style="background:#f8f9fa;padding:14px;text-align:center;">
                <p style="color:#999;font-size:12px;margin:0;">
                    SprinVoices — Tous droits réservés
                </p>
            </div>
        </div>
        """.formatted(
            quote.getCustomer().getName(),
            quote.getNumber(),
            quote.total()
        );
}

private String buildQuoteConvertedEmail(Quote quote) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#ffffff;border:1px solid #eee;border-radius:8px;overflow:hidden;">

            <div style="background:#2980b9;padding:24px;">
                <h1 style="color:white;margin:0;font-size:20px;">SprinVoices</h1>
                <p style="color:#d6eaf8;margin:4px 0 0;font-size:13px;">Transformation devis → facture</p>
            </div>

            <div style="padding:32px;">
                <h2 style="color:#2c3e50;margin-top:0;">Devis converti en facture</h2>

                <p style="color:#555;font-size:14px;">
                    Bonjour <strong>%s</strong>,
                </p>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Votre devis <strong>%s</strong> a été accepté et transformé en facture.
                </p>

                <p style="color:#777;font-size:13px;">
                    Vous pouvez retrouver votre facture dans votre espace client.
                </p>
            </div>

            <div style="background:#f8f9fa;padding:14px;text-align:center;">
                <p style="color:#999;font-size:12px;margin:0;">
                    SprinVoices — Tous droits réservés
                </p>
            </div>
        </div>
        """.formatted(
            quote.getCustomer().getName(),
            quote.getNumber()
        );
}

@Async
public void sendPasswordResetEmail(String to, String resetLink) {
    String subject = "Réinitialisation de votre mot de passe — SprinVoices";
    String body = buildPasswordResetEmail(resetLink);
    sendHtmlEmail(to, subject, body);
}

private String buildPasswordResetEmail(String resetLink) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;background:#ffffff;border:1px solid #eee;border-radius:8px;overflow:hidden;">

            <div style="background:#C0392B;padding:24px;">
                <h1 style="color:white;margin:0;font-size:20px;">SprinVoices</h1>
                <p style="color:#f5f5f5;margin:4px 0 0;font-size:13px;">Sécurité du compte</p>
            </div>

            <div style="padding:32px;">
                <h2 style="color:#2c3e50;margin-top:0;">Réinitialisation du mot de passe</h2>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Vous avez demandé la réinitialisation de votre mot de passe.
                    Cliquez sur le bouton ci-dessous pour en choisir un nouveau.
                </p>

                <p style="color:#555;font-size:14px;line-height:1.6;">
                    Ce lien est valable <strong>30 minutes</strong>.
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.
                </p>

                <div style="text-align:center;margin:32px 0;">
                    <a href="%s"
                       style="background:#C0392B;color:white;padding:14px 28px;border-radius:6px;
                              text-decoration:none;font-size:15px;font-weight:bold;">
                        Réinitialiser mon mot de passe
                    </a>
                </div>

                <p style="color:#aaa;font-size:12px;">
                    Ou copiez ce lien dans votre navigateur :<br>
                    <span style="color:#C0392B;">%s</span>
                </p>
            </div>

            <div style="background:#f8f9fa;padding:14px;text-align:center;">
                <p style="color:#999;font-size:12px;margin:0;">
                    SprinVoices — Tous droits réservés
                </p>
            </div>
        </div>
        """.formatted(resetLink, resetLink);
}
}