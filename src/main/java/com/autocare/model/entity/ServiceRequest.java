package com.autocare.model.entity;

import com.autocare.model.enums.ServiceRequestState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Service request raised by a VehicleOwner.
 *
 * State machine: PENDING → IN_PROGRESS → COMPLETED
 *                PENDING or IN_PROGRESS → CANCELLED
 *
 * On COMPLETED: fires ServiceCompletionEvent (Observer pattern),
 * which triggers ReminderService to close the active reminder
 * and ReminderFactory to create the next one.
 */
@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private VehicleOwner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private ServiceStaff assignedStaff;

    @OneToOne(mappedBy = "serviceRequest", cascade = CascadeType.ALL)
    private ServiceRecord serviceRecord;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String staffNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServiceRequestState state = ServiceRequestState.PENDING;

    @Column
    private String cancellationReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getStateBadgeClass() {
        return switch (state) {
            case PENDING     -> "badge-pending";
            case IN_PROGRESS -> "badge-info";
            case COMPLETED   -> "badge-success";
            case CANCELLED   -> "badge-secondary";
        };
    }
}