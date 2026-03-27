package com.autocare.service.impl;

import com.autocare.factory.ReminderFactory;
import com.autocare.model.entity.*;
import com.autocare.model.enums.*;
import com.autocare.repository.*;
import com.autocare.service.ReminderService;
import com.autocare.service.policy.MaintenancePolicyStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Strategy resolved at runtime from Spring-managed Map<beanName, strategy>.
 * No if-else or switch — adding a new strategy auto-registers it. (OCP)
 */
@Service
@Slf4j
@Transactional
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository                      reminderRepository;
    private final VehicleRepository                       vehicleRepository;
    private final ReminderFactory                         reminderFactory;
    private final Map<String, MaintenancePolicyStrategy>  strategyMap;

    public ReminderServiceImpl(
            ReminderRepository reminderRepository,
            VehicleRepository vehicleRepository,
            ReminderFactory reminderFactory,
            Map<String, MaintenancePolicyStrategy> strategyMap) {
        this.reminderRepository = reminderRepository;
        this.vehicleRepository  = vehicleRepository;
        this.reminderFactory    = reminderFactory;
        this.strategyMap        = strategyMap;
    }

    @Override
    public Reminder createForVehicle(Vehicle vehicle) {
        return reminderRepository.save(reminderFactory.createReminder(vehicle));
    }

    @Override @Transactional(readOnly = true)
    public Optional<Reminder> findActiveReminder(Vehicle vehicle) {
        return reminderRepository.findActiveReminderForVehicle(vehicle);
    }

    @Override @Transactional(readOnly = true)
    public List<Reminder> findAllByOwner(Long ownerId) {
        return reminderRepository.findAllByOwnerId(ownerId);
    }

    @Override @Transactional(readOnly = true)
    public List<Reminder> findByVehicle(Vehicle vehicle) {
        return reminderRepository.findByVehicle(vehicle);
    }

    @Override
    public Reminder evaluateAndUpdate(Reminder reminder) {
        ReminderState old   = reminder.getState();
        ReminderState fresh = computeState(reminder, reminder.getVehicle());
        if (fresh == old) return reminder;

        reminder.setState(fresh);
        Reminder saved = reminderRepository.save(reminder);
        syncVehicleState(reminder.getVehicle(), fresh);
        return saved;
    }

    @Override
    public void evaluateAllActive() {
        reminderRepository.findAllActiveReminders().forEach(this::evaluateAndUpdate);
    }

    @Override
    public Reminder markCompleted(Reminder reminder) {
        reminder.setState(ReminderState.COMPLETED);
        reminder.setCompletedAt(LocalDateTime.now());
        return reminderRepository.save(reminder);
    }

    @Override
    public ReminderState computeState(Reminder reminder, Vehicle vehicle) {
        String key = reminder.getPolicy().getName().toLowerCase() + "PolicyStrategy";
        MaintenancePolicyStrategy strategy = strategyMap.getOrDefault(
                key, strategyMap.get("standardPolicyStrategy"));
        return strategy.evaluateState(reminder, vehicle);
    }

    private void syncVehicleState(Vehicle vehicle, ReminderState state) {
        VehicleState target = switch (state) {
            case OVERDUE, DUE_SOON -> VehicleState.NEEDS_SERVICE;
            case UPCOMING          -> VehicleState.ACTIVE;
            case COMPLETED         -> vehicle.getState();
        };
        if (vehicle.getState() != target &&
            vehicle.getState() != VehicleState.UNDER_SERVICE) {
            vehicle.setState(target);
            vehicleRepository.save(vehicle);
        }
    }
}