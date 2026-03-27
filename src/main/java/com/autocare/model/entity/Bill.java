package com.autocare.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Financial bill for a completed service.
 * Total computed dynamically — not stored to avoid stale data.
 * BillingService solely responsible for creating bills (SRP).
 */
@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_record_id", nullable = false)
    private ServiceRecord serviceRecord;

    @Min(value = 0)
    @Column(nullable = false)
    private Double laborCost;

    @Min(value = 0)
    @Column(nullable = false)
    private Double partsCost;

    @Column(nullable = false)
    private Double taxRate;  // e.g. 18.0 for 18% GST

    @Column
    private String notes;

    @Column(nullable = false)
    private boolean paid = false;

    @Column
    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public Double getSubtotal() {
        return laborCost + partsCost;
    }

    @Transient
    public Double getTaxAmount() {
        return getSubtotal() * (taxRate / 100.0);
    }

    @Transient
    public Double getTotalAmount() {
        return getSubtotal() + getTaxAmount();
    }
}