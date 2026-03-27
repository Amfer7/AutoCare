package com.autocare.service;

import com.autocare.model.dto.*;
import com.autocare.model.entity.*;
import com.autocare.model.enums.ServiceRequestState;
import java.util.List;

public interface ServiceRequestService {
    ServiceRequest create(ServiceRequestDto dto, VehicleOwner owner);
    ServiceRequest findById(Long id);
    List<ServiceRequest> findByOwner(VehicleOwner owner);
    List<ServiceRequest> findByStaff(ServiceStaff staff);
    List<ServiceRequest> findByState(ServiceRequestState state);
    List<ServiceRequest> findAll();
    ServiceRequest assignStaff(Long requestId, Long staffId);
    ServiceRequest startService(Long requestId);
    ServiceRequest completeService(Long requestId, ServiceCompletionDto dto);
    ServiceRequest cancel(Long requestId, String reason);
    void validateNotCompleted(ServiceRequest request);
}