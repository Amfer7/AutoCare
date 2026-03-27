package com.autocare.service.policy;

import com.autocare.model.entity.Reminder;
import com.autocare.model.entity.Vehicle;
import com.autocare.model.enums.ReminderState;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Evaluates reminder state using a HYBRID mileage-OR-date approach.
 * Whichever threshold is crossed first triggers the transition.
 *
 * Decision table:
 * ┌─────────────────────────────────────────────────────┬──────────────┐
 * │ Condition                                           │ State        │
 * ├─────────────────────────────────────────────────────┼──────────────┤
 * │ currentMileage >= dueMileage OR today >= dueDate    │ OVERDUE      │
 * │ currentMileage >= dueSoonMileage OR today>=dueSoon  │ DUE_SOON     │
 * │ Neither threshold crossed                           │ UPCOMING     │
 * │ Reminder already completed                          │ COMPLETED    │
 * └─────────────────────────────────────────────────────┴──────────────┘
 */
@Component("standardPolicyStrategy")
public class StandardPolicyStrategy implements MaintenancePolicyStrategy {

    @Override
    public ReminderState evaluateState(Reminder reminder, Vehicle vehicle) {
        if (reminder.getState() == ReminderState.COMPLETED) {
            return ReminderState.COMPLETED;
        }

        double currentMileage = vehicle.getCurrentMileage();
        LocalDate today       = LocalDate.now();

        boolean mileageOverdue = currentMileage >= reminder.getDueMileage();
        boolean dateOverdue    = !today.isBefore(reminder.getDueDate());
        if (mileageOverdue || dateOverdue) return ReminderState.OVERDUE;

        boolean mileageDueSoon = currentMileage >= reminder.getDueSoonMileage();
        boolean dateDueSoon    = !today.isBefore(reminder.getDueSoonDate());
        if (mileageDueSoon || dateDueSoon) return ReminderState.DUE_SOON;

        return ReminderState.UPCOMING;
    }

    @Override
    public String getPolicyName() { return "Standard"; }
}