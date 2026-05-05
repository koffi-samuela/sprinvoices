package com.example.sprinvoices.controller;

import com.example.sprinvoices.models.Customer;
import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.InvoiceRow;
import com.example.sprinvoices.service.InvoiceService;
import com.example.sprinvoices.service.PdfService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class PdfController {

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private com.example.sprinvoices.repository.CustomerRepository customerRepository;   
    @Autowired
private PdfService pdfService;
    
    // ── Visualiser dans le navigateur ────────────────────────
    @GetMapping("/admin/invoices/{id}/pdf")
    public void viewPdf(@PathVariable Long id, HttpServletResponse response)
            throws IOException, DocumentException {
        generatePdf(id, response, false);
    }

    // ── Télécharger ──────────────────────────────────────────
    @GetMapping("/admin/invoices/{id}/pdf/download")
    public void downloadPdf(@PathVariable Long id, HttpServletResponse response)
            throws IOException, DocumentException {
        generatePdf(id, response, true);
    }
    @GetMapping("/client/invoices/{id}/pdf")
public void viewPdfClient(@PathVariable Long id, HttpServletResponse response,
                           Authentication auth)
        throws IOException, DocumentException {
    // Sécurité : vérifie que la facture appartient au client connecté
    Invoice invoice = invoiceService.findById(id);
    Customer customer = customerRepository.findByUserAccountUsername(auth.getName());
    if (!invoice.getCustomer().getId().equals(customer.getId())) {
        response.sendError(403);
        return;
    }
    generatePdf(id, response, false);
}

@GetMapping("/client/invoices/{id}/pdf/download")
public void downloadPdfClient(@PathVariable Long id, HttpServletResponse response,
                               Authentication auth)
        throws IOException, DocumentException {
    Invoice invoice = invoiceService.findById(id);
    Customer customer = customerRepository.findByUserAccountUsername(auth.getName());
    if (!invoice.getCustomer().getId().equals(customer.getId())) {
        response.sendError(403);
        return;
    }
    generatePdf(id, response, true);
}
    

private void generatePdf(Long id, HttpServletResponse response, boolean download)
        throws IOException, DocumentException {
    Invoice invoice = invoiceService.findById(id); 
    response.setContentType("application/pdf");
    String disposition = download ? "attachment" : "inline";
    response.setHeader("Content-Disposition",
        disposition + "; filename=" + invoice.getNumber() + ".pdf");
    try {
        byte[] pdfBytes = pdfService.generatePdfBytes(invoice);
        response.getOutputStream().write(pdfBytes);
    } catch (Exception e) {
        throw new IOException("Erreur génération PDF", e);
    }
}
}