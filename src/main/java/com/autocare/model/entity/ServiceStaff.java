package com.autocare.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("STAFF")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ServiceStaff extends User {

    @Column
    private String specialization;  // e.g. "Engine", "Tires", "General"

    @OneToMany(mappedBy = "assignedStaff", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceRequest> assignedRequests = new ArrayList<>();
}