package com.autocare.service;

import com.autocare.model.entity.*;
import com.autocare.model.enums.ReminderState;
import java.util.List;
import java.util.Optional;

public interface ReminderService {
    Reminder createForVehicle(Vehicle vehicle);
    Optional<Reminder> findActiveReminder(Vehicle vehicle);
    List<Reminder> findAllByOwner(Long ownerId);
    List<Reminder> findByVehicle(Vehicle vehicle);
    Reminder evaluateAndUpdate(Reminder reminder);
    void evaluateAllActive();         // called by scheduler
    Reminder markCompleted(Reminder reminder);
    ReminderState computeState(Reminder reminder, Vehicle vehicle);
}