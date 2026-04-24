package com.example.sprinvoices.controller;

import com.example.sprinvoices.models.Customer;
import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.Product;
import com.example.sprinvoices.models.Quote;
import com.example.sprinvoices.service.CustomerService;
import com.example.sprinvoices.service.InvoiceService;
import com.example.sprinvoices.service.ProductService;
import com.example.sprinvoices.service.QuoteService;

import java.util.List;
// import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private CustomerService customerService;
    @Autowired private ProductService productService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private QuoteService quoteService;

    // ── Dashboard ────────────────────────────────────────────
@GetMapping("/dashboard")
public String dashboard(Model model) {
    List<Invoice> allInvoices = invoiceService.findAll();

    // Stats générales
    long totalClients  = customerService.findAll().size();
    long totalProducts = productService.findAll().size();
    long totalInvoices = allInvoices.size();

    // Stats par état
    long nbCreated  = allInvoices.stream().filter(i -> i.getInvoicedAt() == null).count();
    long nbInvoiced = allInvoices.stream().filter(i -> i.getInvoicedAt() != null && i.getPaidAt() == null).count();
    long nbPaid     = allInvoices.stream().filter(i -> i.getPaidAt() != null).count();

    // Montants
    double caTotal    = allInvoices.stream().mapToDouble(Invoice::total).sum();
    double caEnAttente = allInvoices.stream()
            .filter(i -> i.getInvoicedAt() != null && i.getPaidAt() == null)
            .mapToDouble(Invoice::total).sum();
    double caPaye     = allInvoices.stream()
            .filter(i -> i.getPaidAt() != null)
            .mapToDouble(Invoice::total).sum();

    // 5 dernières factures
    List<Invoice> recentInvoices = allInvoices.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());

    model.addAttribute("totalClients",   totalClients);
    model.addAttribute("totalProducts",  totalProducts);
    model.addAttribute("totalInvoices",  totalInvoices);
    model.addAttribute("nbCreated",      nbCreated);
    model.addAttribute("nbInvoiced",     nbInvoiced);
    model.addAttribute("nbPaid",         nbPaid);
    model.addAttribute("caTotal",        caTotal);
    model.addAttribute("caEnAttente",    caEnAttente);
    model.addAttribute("caPaye",         caPaye);
    model.addAttribute("recentInvoices", recentInvoices);

    return "admin/dashboard";
}

    // ══════════════════════════════════════════════════════════
    // CLIENTS
    // ══════════════════════════════════════════════════════════
    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "admin/customers/list";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "admin/customers/form";
    }

@PostMapping("/customers/new")
public String createCustomer(@ModelAttribute Customer customer,
                              @RequestParam String username,
                              @RequestParam String password,
                               @RequestParam String role,
                              RedirectAttributes redirectAttributes) {

    try {
        customerService.create(customer, username, password, role);
        return "redirect:/admin/customers";

    } catch (RuntimeException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        redirectAttributes.addFlashAttribute("customer", customer);
        return "redirect:/admin/customers/new";
    }
}

    @GetMapping("/customers/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.findById(id));
        return "admin/customers/form";
    }

    @PostMapping("/customers/edit/{id}")
    public String updateCustomer(@PathVariable Long id, @ModelAttribute Customer customer) {
        customerService.update(id, customer);
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return "redirect:/admin/customers";
    }

    // ══════════════════════════════════════════════════════════
    // PRODUITS
    // ══════════════════════════════════════════════════════════
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products/form";
    }

    @PostMapping("/products/new")
    public String createProduct(@ModelAttribute Product product) {
        productService.create(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "admin/products/form";
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
        productService.update(id, product);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }

    // ══════════════════════════════════════════════════════════
    // FACTURES
    // ══════════════════════════════════════════════════════════
@GetMapping("/invoices")
public String invoices(
        @RequestParam(required = false) Long customerId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        Model model) {

    Pageable pageable = PageRequest.of(page, 1); // 1 facture par page pour tester

    Page<Invoice> invoices = invoiceService.findWithFilters(
            customerId,
            status,
            pageable
    );

    model.addAttribute("invoices", invoices);
    model.addAttribute("customers", customerService.findAll());

    model.addAttribute("selectedCustomer", customerId);
    model.addAttribute("selectedStatus", status);

    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", invoices.getTotalPages());

    return "admin/invoices/list";
}

    @GetMapping("/invoices/new")
    public String newInvoiceForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("customers", customerService.findAll());
        return "admin/invoices/form";
    }

    @PostMapping("/invoices/new")
    public String createInvoice(@ModelAttribute Invoice invoice,
                                 @RequestParam Long customerId) {
        invoice.setCustomer(customerService.findById(customerId));
        Invoice saved = invoiceService.create(invoice);
        return "redirect:/admin/invoices/" + saved.getId();
    }

    @GetMapping("/invoices/{id}")
    public String invoiceDetail(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        model.addAttribute("products", productService.findAll());
        return "admin/invoices/detail";
    }

    @PostMapping("/invoices/{id}/add-row")
    public String addRow(@PathVariable Long id,
                          @RequestParam Long productId,
                          @RequestParam double quantity) {
        invoiceService.addRow(id, productService.findById(productId), quantity);
        return "redirect:/admin/invoices/" + id;
    }

    @PostMapping("/invoices/{id}/delete-row/{rowId}")
    public String deleteRow(@PathVariable Long id, @PathVariable Long rowId) {
        invoiceService.deleteRow(id, rowId);
        return "redirect:/admin/invoices/" + id;
    }

    @PostMapping("/invoices/{id}/mark-invoiced")
    public String markInvoiced(@PathVariable Long id) {
        invoiceService.markAsInvoiced(id);
        return "redirect:/admin/invoices/" + id;
    }

    @PostMapping("/invoices/{id}/mark-paid")
    public String markPaid(@PathVariable Long id) {
        invoiceService.markAsPaid(id);
        return "redirect:/admin/invoices/" + id;
    }

    @PostMapping("/invoices/delete/{id}")
    public String deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return "redirect:/admin/invoices";
    }

    // ══════════════════════════════════════════════════════════
// DEVIS
// ══════════════════════════════════════════════════════════
@GetMapping("/quotes")
public String quotes(Model model) {
    model.addAttribute("quotes", quoteService.findAll());
    return "admin/quotes/list";
}

@GetMapping("/quotes/new")
public String newQuoteForm(Model model) {
    model.addAttribute("quote", new Quote());
    model.addAttribute("customers", customerService.findAll());
    return "admin/quotes/form";
}

@PostMapping("/quotes/new")
public String createQuote(@RequestParam String designation,
                           @RequestParam Long customerId) {
    Quote quote = new Quote();
    quote.setDesignation(designation);
    quote.setCustomer(customerService.findById(customerId));
    Quote saved = quoteService.create(quote);
    return "redirect:/admin/quotes/" + saved.getId();
}

@GetMapping("/quotes/{id}")
public String quoteDetail(@PathVariable Long id, Model model) {
    model.addAttribute("quote", quoteService.findById(id));
    model.addAttribute("products", productService.findAll());
    return "admin/quotes/detail";
}

@PostMapping("/quotes/{id}/add-row")
public String addQuoteRow(@PathVariable Long id,
                           @RequestParam Long productId,
                           @RequestParam double quantity) {
    quoteService.addRow(id, productService.findById(productId), quantity);
    return "redirect:/admin/quotes/" + id;
}

@PostMapping("/quotes/{id}/delete-row/{rowId}")
public String deleteQuoteRow(@PathVariable Long id, @PathVariable Long rowId) {
    quoteService.deleteRow(id, rowId);
    return "redirect:/admin/quotes/" + id;
}

@PostMapping("/quotes/{id}/send")
public String sendQuote(@PathVariable Long id) {
    quoteService.markAsSent(id);
    return "redirect:/admin/quotes/" + id;
}

@PostMapping("/quotes/{id}/accept")
public String acceptQuote(@PathVariable Long id) {
    quoteService.markAsAccepted(id);
    return "redirect:/admin/quotes/" + id;
}

@PostMapping("/quotes/{id}/refuse")
public String refuseQuote(@PathVariable Long id) {
    quoteService.markAsRefused(id);
    return "redirect:/admin/quotes/" + id;
}

@PostMapping("/quotes/{id}/convert")
public String convertQuote(@PathVariable Long id) {
    Invoice invoice = quoteService.convertToInvoice(id);
    return "redirect:/admin/invoices/" + invoice.getId();
}

@PostMapping("/quotes/delete/{id}")
public String deleteQuote(@PathVariable Long id) {
    quoteService.delete(id);
    return "redirect:/admin/quotes";
}
}

