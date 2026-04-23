package com.example.sprinvoices.controller;

import com.example.sprinvoices.dto.InvoiceDTO;
import com.example.sprinvoices.dto.InvoiceRowDTO;
import com.example.sprinvoices.models.Customer;
import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.InvoiceRow;
import com.example.sprinvoices.repository.CustomerRepository;
import com.example.sprinvoices.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InvoiceApiController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CustomerRepository customerRepository;

    // ── Convertit Invoice → InvoiceDTO ───────────────────────
    private InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setDesignation(invoice.getDesignation());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setInvoicedAt(invoice.getInvoicedAt());
        dto.setPaidAt(invoice.getPaidAt());
        dto.setTotal(invoice.total());
        dto.setStatus(invoice.getStatus());

        List<InvoiceRowDTO> rowDTOs = invoice.getRows().stream().map(row -> {
            InvoiceRowDTO rowDTO = new InvoiceRowDTO();
            rowDTO.setId(row.getId());
            rowDTO.setProductDesignation(row.getProduct().getDesignation());
            rowDTO.setProductCategory(row.getProduct().getCategory());
            rowDTO.setUnitPrice(row.getProduct().getUnitPrice());
            rowDTO.setQuantity(row.getQuantity());
            rowDTO.setAmount(row.amount());
            return rowDTO;
        }).collect(Collectors.toList());

        dto.setRows(rowDTOs);
        return dto;
    }

    // ── GET /api/invoices ─────────────────────────────────────
    @GetMapping("/invoices")
    public ResponseEntity<?> getInvoices(Authentication auth) {
        Customer customer = customerRepository.findByUserAccountUsername(auth.getName());

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Accès réservé aux clients.\"}");
        }

        List<InvoiceDTO> dtos = invoiceService.findByCustomerId(customer.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ── GET /api/invoices/{id} ────────────────────────────────
    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable Long id, Authentication auth) {
        Customer customer = customerRepository.findByUserAccountUsername(auth.getName());

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Accès réservé aux clients.\"}");
        }

        Invoice invoice = invoiceService.findById(id);

        // Vérifie que la facture appartient bien au client connecté
        if (!invoice.getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Cette facture ne vous appartient pas.\"}");
        }

        return ResponseEntity.ok(toDTO(invoice));
    }
}