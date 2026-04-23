package com.example.sprinvoices.service;

import com.example.sprinvoices.models.*;
import com.example.sprinvoices.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteService {

    @Autowired private QuoteRepository quoteRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired
private EmailService emailService;
    public List<Quote> findAll() {
        return quoteRepository.findAll();
    }

    public Quote findById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis introuvable : " + id));
    }

    public List<Quote> findByCustomerId(Long customerId) {
        return quoteRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Quote create(Quote quote) {
        quote.setCreatedAt(LocalDateTime.now());
        quote.setStatus("DRAFT");

        // Numéro automatique DEV-2026-001
        int year = LocalDateTime.now().getYear();
        long count = quoteRepository.count() + 1;
        quote.setNumber(String.format("DEV-%d-%03d", year, count));

        return quoteRepository.save(quote);
    }

    @Transactional
    public void addRow(Long quoteId, Product product, double quantity) {
        Quote quote = findById(quoteId);
        QuoteRow row = new QuoteRow();
        row.setQuote(quote);
        row.setProduct(product);
        row.setQuantity(quantity);
        quote.getRows().add(row);
        quoteRepository.save(quote);
    }

    @Transactional
    public void deleteRow(Long quoteId, Long rowId) {
        Quote quote = findById(quoteId);
        quote.getRows().removeIf(r -> r.getId().equals(rowId));
        quoteRepository.save(quote);
    }

    @Transactional
    public void markAsSent(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus().equals("DRAFT")) {
            quote.setStatus("SENT");
            quote.setSentAt(LocalDateTime.now());
            quoteRepository.save(quote);
              emailService.sendQuoteSentNotification(quote);
        }
    }

    @Transactional
    public void markAsAccepted(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus().equals("SENT")) {
            quote.setStatus("ACCEPTED");
            quoteRepository.save(quote);
        }
    }

    @Transactional
    public void markAsRefused(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus().equals("SENT")) {
            quote.setStatus("REFUSED");
            quoteRepository.save(quote);
        }
    }

    // Convertit le devis en facture
@Transactional
public Invoice convertToInvoice(Long quoteId) {
    Quote quote = findById(quoteId);

    Invoice invoice = new Invoice();
    invoice.setDesignation(quote.getDesignation());
    invoice.setCustomer(quote.getCustomer());
    invoice.setCreatedAt(LocalDateTime.now());

    int year = LocalDateTime.now().getYear();
    long count = invoiceRepository.count() + 1;
    invoice.setNumber(String.format("FAC-%d-%03d", year, count));

    for (QuoteRow qRow : quote.getRows()) {
        InvoiceRow iRow = new InvoiceRow();
        iRow.setInvoice(invoice);
        iRow.setProduct(qRow.getProduct());
        iRow.setQuantity(qRow.getQuantity());
        invoice.getRows().add(iRow);
    }

    quote.setStatus("ACCEPTED");
    quoteRepository.save(quote);

    Invoice saved = invoiceRepository.save(invoice);
    emailService.sendQuoteConvertedNotification(quote); // ← ajout
    return saved;
}

    @Transactional
    public void delete(Long id) {
        quoteRepository.deleteById(id);
    }
}