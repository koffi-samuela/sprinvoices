package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Invoice;
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
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;">
                <div style="background:#C0392B;padding:24px;border-radius:8px 8px 0 0;">
                    <h1 style="color:white;margin:0;font-size:22px;">SprinVoices</h1>
                </div>
                <div style="background:white;padding:32px;border:1px solid #eee;">
                    <h2 style="color:#2c3e50;">Votre facture est disponible</h2>
                    <p style="color:#555;">Bonjour <strong>%s</strong>,</p>
                    <p style="color:#555;">Votre facture <strong>%s</strong> d'un montant de <strong>%.2f €</strong> HT est disponible.</p>
                    <div style="background:#f8f9fa;padding:16px;border-radius:6px;margin:24px 0;">
                        <p style="margin:0;color:#555;">Délai de paiement : <strong>%d jours</strong></p>
                    </div>
                    <p style="color:#555;">Connectez-vous à votre espace client pour la consulter.</p>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-radius:0 0 8px 8px;">
                    <p style="color:#999;font-size:12px;margin:0;">SprinVoices — Module de facturation</p>
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
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;">
                <div style="background:#27ae60;padding:24px;border-radius:8px 8px 0 0;">
                    <h1 style="color:white;margin:0;font-size:22px;">SprinVoices</h1>
                </div>
                <div style="background:white;padding:32px;border:1px solid #eee;">
                    <h2 style="color:#2c3e50;">✅ Paiement confirmé</h2>
                    <p style="color:#555;">Bonjour <strong>%s</strong>,</p>
                    <p style="color:#555;">Nous confirmons la réception du paiement de votre facture <strong>%s</strong> d'un montant de <strong>%.2f €</strong> HT.</p>
                    <p style="color:#555;">Merci pour votre règlement.</p>
                </div>
                <div style="background:#f8f9fa;padding:16px;text-align:center;border-radius:0 0 8px 8px;">
                    <p style="color:#999;font-size:12px;margin:0;">SprinVoices — Module de facturation</p>
                </div>
            </div>
            """.formatted(
                invoice.getCustomer().getName(),
                invoice.getNumber(),
                invoice.total()
            );
    }
    
    
}