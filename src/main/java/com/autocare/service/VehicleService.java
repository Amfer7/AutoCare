package com.autocare.service;

import com.autocare.model.dto.*;
import com.autocare.model.entity.*;
import com.autocare.model.enums.VehicleState;
import java.util.List;

public interface VehicleService {
    Vehicle register(VehicleRegistrationDto dto, VehicleOwner owner);
    Vehicle findById(Long id);
    List<Vehicle> findByOwner(VehicleOwner owner);
    List<Vehicle> findByState(VehicleState state);
    Vehicle updateMileage(Long vehicleId, MileageUpdateDto dto, String updatedByEmail);
    Vehicle updateState(Long vehicleId, VehicleState newState);
    void validateOwnership(Long vehicleId, Long ownerId);
}