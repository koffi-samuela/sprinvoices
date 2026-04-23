package com.example.sprinvoices.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDTO {
    private Long id;
    private String designation;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime invoicedAt;
    private LocalDateTime paidAt;
    private double total;
    private List<InvoiceRowDTO> rows;
}