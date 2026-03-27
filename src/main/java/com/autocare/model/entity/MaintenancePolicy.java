package com.autocare.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Persisted configuration for a maintenance policy.
 *
 * DATA lives here. BEHAVIOUR lives in MaintenancePolicyStrategy implementations.
 * This separation satisfies SRP cleanly.
 *
 * OCP: new policy types → new strategy implementations, no existing code changed.
 * DIP: services depend on MaintenancePolicyStrategy interface, not this entity directly.
 */
@Entity
@Table(name = "maintenance_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 100)
    @Column(nullable = false)
    private Double mileageInterval;           // km between services

    @Column(nullable = false)
    private Double mileageDueSoonThreshold;   // km before due to warn

    @Min(value = 1)
    @Column(nullable = false)
    private Integer timeIntervalDays;         // days between services

    @Column(nullable = false)
    private Integer timeDueSoonThresholdDays; // days before due to warn

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}