package com.autocare.model.entity;

import com.autocare.model.enums.VehicleState;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle registered in the system.
 *
 * Mileage invariant: currentMileage must always be >= previousMileage.
 * Enforced at the service layer for rich error messaging.
 *
 * State transitions (managed by VehicleService):
 *   ACTIVE        → NEEDS_SERVICE  (when reminder becomes DUE_SOON or OVERDUE)
 *   NEEDS_SERVICE → UNDER_SERVICE  (when ServiceRequest created)
 *   UNDER_SERVICE → ACTIVE         (when ServiceRequest COMPLETED)
 */
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "License plate is required")
    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @NotBlank(message = "Make is required")
    @Column(nullable = false)
    private String make;

    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;

    @Min(value = 1900)
    @Max(value = 2100)
    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private String color;

    @Min(value = 0)
    @Column(nullable = false)
    private Double currentMileage = 0.0;

    @Column(nullable = false)
    private Double previousMileage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleState state = VehicleState.ACTIVE;

    @Column
    private String fuelType;

    @Column
    private String vinNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private VehicleOwner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private MaintenancePolicy maintenancePolicy;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reminder> reminders = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceRequest> serviceRequests = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        updatedAt    = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        return year + " " + make + " " + model;
    }
}