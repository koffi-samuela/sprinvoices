package com.example.sprinvoices.dto;

import lombok.Data;

@Data
public class InvoiceRowDTO {
    private Long id;
    private String productDesignation;
    private String productCategory;
    private double unitPrice;
    private double quantity;
    private double amount;
}