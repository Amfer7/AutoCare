package com.autocare.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillDto {
    private Long billId;
    private Long serviceRequestId;
    private String vehicleDisplayName;
    private String ownerName;
    private String staffName;
    private Double laborCost;
    private Double partsCost;
    private Double taxRate;
    private Double subtotal;
    private Double taxAmount;
    private Double totalAmount;
    private boolean paid;
    private String generatedAt;
    private String paidAt;
    private String notes;
}