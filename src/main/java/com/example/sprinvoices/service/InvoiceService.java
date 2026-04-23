package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.InvoiceRow;
import com.example.sprinvoices.models.Product;
import com.example.sprinvoices.repository.InvoiceRepository;
import com.example.sprinvoices.repository.InvoiceRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    
    @Autowired
    private EmailService emailService;  

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable : " + id));
    }

    public List<Invoice> findByCustomerId(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

@Transactional
public Invoice create(Invoice invoice) {
    invoice.setCreatedAt(LocalDateTime.now());

    // Génère FAC-2026-001
    int year = LocalDateTime.now().getYear();
    long count = invoiceRepository.countByYear(year) + 1;
    invoice.setNumber(String.format("FAC-%d-%03d", year, count));

    return invoiceRepository.save(invoice);
}
    // Ajoute une ligne à une facture existante
    @Transactional
    public void addRow(Long invoiceId, Product product, double quantity) {
        Invoice invoice = findById(invoiceId);
        InvoiceRow row = new InvoiceRow();
        row.setInvoice(invoice);
        row.setProduct(product);
        row.setQuantity(quantity);
        invoice.getRows().add(row);
        invoiceRepository.save(invoice);
    }

    // Supprime une ligne
    @Transactional
    public void deleteRow(Long invoiceId, Long rowId) {
        Invoice invoice = findById(invoiceId);
        invoice.getRows().removeIf(r -> r.getId().equals(rowId));
        invoiceRepository.save(invoice);
    }

    // Passe la facture en état FACTURÉE
    @Transactional
    public void markAsInvoiced(Long id) {
        Invoice invoice = findById(id);
        if (invoice.getInvoicedAt() == null) {
            invoice.setInvoicedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            emailService.sendInvoicedNotification(invoice);
        }
    }

    // Passe la facture en état PAYÉE
    @Transactional
    public void markAsPaid(Long id) {
        Invoice invoice = findById(id);
        if (invoice.getInvoicedAt() != null && invoice.getPaidAt() == null) {
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            emailService.sendPaidConfirmation(invoice);
        }
    }

    @Transactional
    public void delete(Long id) {
        invoiceRepository.deleteById(id);
    }

    public List<Invoice> findWithFilters(Long customerId, String status) {
    // Si les deux filtres sont vides on retourne tout
    String statusParam = (status != null && !status.isEmpty()) ? status : null;
    Long customerParam = (customerId != null && customerId != 0) ? customerId : null;
    return invoiceRepository.findWithFilters(customerParam, statusParam);
}
}