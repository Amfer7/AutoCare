package com.autocare.service.impl;

import com.autocare.model.dto.*;
import com.autocare.model.entity.*;
import com.autocare.model.enums.VehicleState;
import com.autocare.repository.*;
import com.autocare.factory.ReminderFactory;
import com.autocare.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository           vehicleRepository;
    private final MaintenancePolicyRepository policyRepository;
    private final ReminderFactory             reminderFactory;
    private final ReminderService             reminderService;
    private final ReminderRepository          reminderRepository;

    @Override
    public Vehicle register(VehicleRegistrationDto dto, VehicleOwner owner) {
        if (vehicleRepository.existsByLicensePlate(dto.getLicensePlate()))
            throw new IllegalArgumentException(
                "Vehicle with plate [" + dto.getLicensePlate() + "] already exists.");

        MaintenancePolicy policy = policyRepository.findById(dto.getPolicyId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Policy not found: " + dto.getPolicyId()));

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(dto.getLicensePlate().toUpperCase().trim())
                .make(dto.getMake()).model(dto.getModel()).year(dto.getYear())
                .color(dto.getColor()).fuelType(dto.getFuelType())
                .vinNumber(dto.getVinNumber())
                .currentMileage(dto.getCurrentMileage())
                .previousMileage(dto.getCurrentMileage())
                .state(VehicleState.ACTIVE)
                .owner(owner).maintenancePolicy(policy)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        // Auto-create first reminder via Factory
        reminderRepository.save(reminderFactory.createReminder(saved));
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<Vehicle> findByOwner(VehicleOwner owner) {
        return vehicleRepository.findByOwner(owner);
    }

    @Override @Transactional(readOnly = true)
    public List<Vehicle> findByState(VehicleState state) {
        return vehicleRepository.findByState(state);
    }

    @Override
    public Vehicle updateMileage(Long vehicleId, MileageUpdateDto dto, String updatedByEmail) {
        Vehicle vehicle = findById(vehicleId);

        if (dto.getNewMileage() < vehicle.getCurrentMileage())
            throw new IllegalArgumentException(
                "New mileage (" + dto.getNewMileage() + ") cannot be less than " +
                "current (" + vehicle.getCurrentMileage() + "). Mileage only increases.");

        if (dto.getNewMileage().equals(vehicle.getCurrentMileage()))
            throw new IllegalArgumentException("New mileage equals current mileage.");

        vehicle.setPreviousMileage(vehicle.getCurrentMileage());
        vehicle.setCurrentMileage(dto.getNewMileage());
        Vehicle saved = vehicleRepository.save(vehicle);

        // Immediately re-evaluate active reminder after mileage change
        reminderService.findActiveReminder(saved).ifPresent(reminderService::evaluateAndUpdate);
        return saved;
    }

    @Override
    public Vehicle updateState(Long vehicleId, VehicleState newState) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setState(newState);
        return vehicleRepository.save(vehicle);
    }

    @Override @Transactional(readOnly = true)
    public void validateOwnership(Long vehicleId, Long ownerId) {
        Vehicle vehicle = findById(vehicleId);
        if (!vehicle.getOwner().getId().equals(ownerId))
            throw new SecurityException(
                "Access denied: you do not own vehicle [" + vehicleId + "].");
    }
}