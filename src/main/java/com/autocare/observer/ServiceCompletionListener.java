package com.autocare.observer;

import com.autocare.factory.ReminderFactory;
import com.autocare.model.entity.*;
import com.autocare.model.enums.VehicleState;
import com.autocare.repository.*;
import com.autocare.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * OBSERVER PATTERN — Listener (Observer)
 * Reacts to ServiceCompletionEvent:
 *   1. Marks active reminder COMPLETED
 *   2. Creates next reminder (new cycle)
 *   3. Resets vehicle state to ACTIVE
 *
 * Completely transparent to ServiceRequestService. (OCP, DIP)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceCompletionListener {

    private final ReminderService    reminderService;
    private final ReminderFactory    reminderFactory;
    private final ReminderRepository reminderRepository;
    private final VehicleRepository  vehicleRepository;

    @EventListener
    @Transactional
    public void onServiceCompleted(ServiceCompletionEvent event) {
        ServiceRecord record  = event.getServiceRecord();
        Vehicle       vehicle = record.getServiceRequest().getVehicle();

        // 1. Close active reminder
        Optional<Reminder> active = reminderService.findActiveReminder(vehicle);
        active.ifPresent(reminderService::markCompleted);

        // 2. Start next reminder cycle
        Reminder next = reminderFactory.createNextReminder(
                vehicle, record.getMileageAtService());
        reminderRepository.save(next);

        // 3. Reset vehicle to ACTIVE
        vehicle.setState(VehicleState.ACTIVE);
        vehicleRepository.save(vehicle);

        log.info("Post-completion: reminder cycled, vehicle [{}] → ACTIVE",
                 vehicle.getLicensePlate());
    }
}