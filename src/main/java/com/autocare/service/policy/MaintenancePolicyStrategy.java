package com.autocare.service.policy;

import com.autocare.model.entity.Reminder;
import com.autocare.model.entity.Vehicle;
import com.autocare.model.enums.ReminderState;

public interface MaintenancePolicyStrategy {
    ReminderState evaluateState(Reminder reminder, Vehicle vehicle);
    String getPolicyName();
}