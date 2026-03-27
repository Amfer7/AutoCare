package com.autocare.factory;

import com.autocare.model.entity.*;
import com.autocare.model.enums.ReminderState;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FACTORY PATTERN
 * Centralises Reminder construction — callers never instantiate Reminder directly.
 * SRP: solely responsible for Reminder instantiation.
 * OCP: if threshold logic changes, only this class changes.
 */
@Component
public class ReminderFactory {

    /** Creates first reminder for a newly registered vehicle. */
    public Reminder createReminder(Vehicle vehicle) {
        MaintenancePolicy policy = vehicle.getMaintenancePolicy();
        if (policy == null) throw new IllegalStateException(
            "Vehicle [" + vehicle.getLicensePlate() + "] has no maintenance policy assigned.");

        double baselineMileage = vehicle.getCurrentMileage();
        LocalDate today        = LocalDate.now();

        return Reminder.builder()
                .vehicle(vehicle)
                .policy(policy)
                .state(ReminderState.UPCOMING)
                .baselineMileage(baselineMileage)
                .dueMileage(baselineMileage + policy.getMileageInterval())
                .dueSoonMileage(baselineMileage + policy.getMileageInterval()
                                - policy.getMileageDueSoonThreshold())
                .createdDate(today)
                .dueDate(today.plusDays(policy.getTimeIntervalDays()))
                .dueSoonDate(today.plusDays(policy.getTimeIntervalDays())
                             .minusDays(policy.getTimeDueSoonThresholdDays()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /** Creates the NEXT reminder after a service is completed. */
    public Reminder createNextReminder(Vehicle vehicle, double mileageAtService) {
        MaintenancePolicy policy = vehicle.getMaintenancePolicy();
        if (policy == null) throw new IllegalStateException(
            "Vehicle [" + vehicle.getLicensePlate() + "] has no maintenance policy assigned.");

        LocalDate today = LocalDate.now();

        return Reminder.builder()
                .vehicle(vehicle)
                .policy(policy)
                .state(ReminderState.UPCOMING)
                .baselineMileage(mileageAtService)
                .dueMileage(mileageAtService + policy.getMileageInterval())
                .dueSoonMileage(mileageAtService + policy.getMileageInterval()
                                - policy.getMileageDueSoonThreshold())
                .createdDate(today)
                .dueDate(today.plusDays(policy.getTimeIntervalDays()))
                .dueSoonDate(today.plusDays(policy.getTimeIntervalDays())
                             .minusDays(policy.getTimeDueSoonThresholdDays()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}