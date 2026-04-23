package com.example.sprinvoices.controller;

import com.example.sprinvoices.models.Customer;
import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.repository.CustomerRepository;
import com.example.sprinvoices.service.InvoiceService;
import com.example.sprinvoices.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CustomerRepository customerRepository;

    // Récupère le customer lié au user connecté
    private Customer getCustomer(Authentication auth) {
        return customerRepository.findByUserAccountUsername(auth.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Customer customer = getCustomer(auth);
        List<Invoice> invoices = invoiceService.findByCustomerId(customer.getId());
        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoices);
        return "client/dashboard";
    }

    @GetMapping("/invoices/{id}")
    public String invoiceDetail(@PathVariable Long id, Authentication auth, Model model) {
        Invoice invoice = invoiceService.findById(id);

        // Sécurité : un client ne peut voir que SES factures
        Customer customer = getCustomer(auth);
        if (!invoice.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/client/dashboard";
        }

        model.addAttribute("invoice", invoice);
        return "client/invoice-detail";
    }
}