package com.autocare.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Immutable record of a completed service.
 * SRP: stores WHAT was done. Bill stores WHAT it cost.
 */
@Entity
@Table(name = "service_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @OneToOne(mappedBy = "serviceRecord", cascade = CascadeType.ALL)
    private Bill bill;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String workPerformed;

    @Column(nullable = false)
    private Double mileageAtService;

    @Column(columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(columnDefinition = "TEXT")
    private String technicianNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}