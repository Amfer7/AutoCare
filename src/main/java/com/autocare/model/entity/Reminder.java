package com.autocare.model.entity;

import com.autocare.model.enums.ReminderState;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maintenance reminder for a vehicle.
 * Created by ReminderFactory. State evaluated by ReminderScheduler via Strategy.
 *
 * Hybrid trigger: whichever threshold (mileage or date) is crossed first
 * triggers the state transition, evaluated in StandardPolicyStrategy.
 *
 * State machine: UPCOMING → DUE_SOON → OVERDUE → COMPLETED → (cycle restarts)
 */
@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private MaintenancePolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReminderState state = ReminderState.UPCOMING;

    // ── Mileage thresholds ────────────────────────────────────────────────────
    @Column(nullable = false)
    private Double baselineMileage;       // snapshot at reminder creation

    @Column(nullable = false)
    private Double dueMileage;            // baselineMileage + policy.mileageInterval

    @Column(nullable = false)
    private Double dueSoonMileage;        // dueMileage - policy.mileageDueSoonThreshold

    // ── Date thresholds ───────────────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDate createdDate;

    @Column(nullable = false)
    private LocalDate dueDate;            // createdDate + policy.timeIntervalDays

    @Column(nullable = false)
    private LocalDate dueSoonDate;        // dueDate - policy.timeDueSoonThresholdDays

    // ── Completion ────────────────────────────────────────────────────────────
    @Column
    private LocalDateTime completedAt;

    @Column
    private String notes;

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

    public boolean isActive() {
        return state != ReminderState.COMPLETED;
    }

    public String getUrgencyLabel() {
        return switch (state) {
            case UPCOMING  -> "Upcoming";
            case DUE_SOON  -> "Due Soon";
            case OVERDUE   -> "Overdue";
            case COMPLETED -> "Completed";
        };
    }

    public String getBadgeClass() {
        return switch (state) {
            case UPCOMING  -> "badge-upcoming";
            case DUE_SOON  -> "badge-warning";
            case OVERDUE   -> "badge-danger";
            case COMPLETED -> "badge-success";
        };
    }
}