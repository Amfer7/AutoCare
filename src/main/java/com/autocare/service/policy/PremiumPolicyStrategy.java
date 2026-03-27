package com.autocare.service.policy;

import com.autocare.model.entity.*;
import com.autocare.model.enums.ReminderState;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Applies stricter thresholds — triggers earlier warnings.
 * Added WITHOUT touching StandardPolicyStrategy or any service code. (OCP)
 */
@Component("premiumPolicyStrategy")
public class PremiumPolicyStrategy implements MaintenancePolicyStrategy {

    private static final double EARLY_WARNING_FACTOR = 0.90;

    @Override
    public ReminderState evaluateState(Reminder reminder, Vehicle vehicle) {
        if (reminder.getState() == ReminderState.COMPLETED) return ReminderState.COMPLETED;

        double currentMileage = vehicle.getCurrentMileage();
        LocalDate today       = LocalDate.now();

        double adjustedDueMileage = reminder.getDueMileage() * EARLY_WARNING_FACTOR;
        LocalDate adjustedDueDate = reminder.getDueDate().minusDays(5);

        if (currentMileage >= adjustedDueMileage || !today.isBefore(adjustedDueDate))
            return ReminderState.OVERDUE;

        double adjustedDueSoonMileage = reminder.getDueSoonMileage() * EARLY_WARNING_FACTOR;
        LocalDate adjustedDueSoonDate = reminder.getDueSoonDate().minusDays(5);

        if (currentMileage >= adjustedDueSoonMileage || !today.isBefore(adjustedDueSoonDate))
            return ReminderState.DUE_SOON;

        return ReminderState.UPCOMING;
    }

    @Override
    public String getPolicyName() { return "Premium"; }
}