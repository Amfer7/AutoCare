package com.autocare.service.impl;

import com.autocare.model.dto.*;
import com.autocare.model.entity.*;
import com.autocare.model.enums.*;
import com.autocare.observer.ServiceCompletionEvent;
import com.autocare.repository.*;
import com.autocare.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRecordRepository  serviceRecordRepository;
    private final VehicleRepository        vehicleRepository;
    private final VehicleService           vehicleService;
    private final UserService              userService;
    private final BillingService           billingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ServiceRequest create(ServiceRequestDto dto, VehicleOwner owner) {
        Vehicle vehicle = vehicleService.findById(dto.getVehicleId());
        vehicleService.validateOwnership(dto.getVehicleId(), owner.getId());

        boolean openExists = serviceRequestRepository
                .findByOwnerOrderByRequestedAtDesc(owner).stream()
                .anyMatch(r -> r.getVehicle().getId().equals(vehicle.getId())
                    && (r.getState() == ServiceRequestState.PENDING
                     || r.getState() == ServiceRequestState.IN_PROGRESS));

        if (openExists) throw new IllegalStateException(
            "Open request already exists for [" + vehicle.getLicensePlate() + "].");

        ServiceRequest req = ServiceRequest.builder()
                .vehicle(vehicle).owner(owner)
                .description(dto.getDescription())
                .state(ServiceRequestState.PENDING).build();
        ServiceRequest saved = serviceRequestRepository.save(req);

        vehicle.setState(VehicleState.UNDER_SERVICE);
        vehicleRepository.save(vehicle);
        return saved;
    }

    @Override @Transactional(readOnly = true)
    public ServiceRequest findById(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Service request not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<ServiceRequest> findByOwner(VehicleOwner owner) {
        return serviceRequestRepository.findByOwnerOrderByRequestedAtDesc(owner);
    }

    @Override @Transactional(readOnly = true)
    public List<ServiceRequest> findByStaff(ServiceStaff staff) {
        return serviceRequestRepository.findByAssignedStaffOrderByRequestedAtDesc(staff);
    }

    @Override @Transactional(readOnly = true)
    public List<ServiceRequest> findByState(ServiceRequestState state) {
        return serviceRequestRepository.findByStateOrderByRequestedAtDesc(state);
    }

    @Override @Transactional(readOnly = true)
    public List<ServiceRequest> findAll() { return serviceRequestRepository.findAll(); }

    @Override
    public ServiceRequest assignStaff(Long requestId, Long staffId) {
        ServiceRequest req = findById(requestId);
        validateNotCompleted(req);
        req.setAssignedStaff(userService.getStaffById(staffId));
        return serviceRequestRepository.save(req);
    }

    @Override
    public ServiceRequest startService(Long requestId) {
        ServiceRequest req = findById(requestId);
        if (req.getState() != ServiceRequestState.PENDING)
            throw new IllegalStateException("Only PENDING requests can be started.");
        req.setState(ServiceRequestState.IN_PROGRESS);
        req.setStartedAt(LocalDateTime.now());
        return serviceRequestRepository.save(req);
    }

    @Override
    public ServiceRequest completeService(Long requestId, ServiceCompletionDto dto) {
        ServiceRequest req = findById(requestId);
        if (req.getState() != ServiceRequestState.IN_PROGRESS)
            throw new IllegalStateException("Only IN_PROGRESS requests can be completed.");

        // 1. Create ServiceRecord
        ServiceRecord record = serviceRecordRepository.save(ServiceRecord.builder()
                .serviceRequest(req)
                .workPerformed(dto.getWorkPerformed())
                .mileageAtService(dto.getMileageAtService())
                .partsReplaced(dto.getPartsReplaced())
                .technicianNotes(dto.getTechnicianNotes())
                .build());

        // 2. Generate Bill
        billingService.generateBill(record, dto.getLaborCost(),
                                    dto.getPartsCost(), dto.getBillNotes());

        // 3. Update vehicle mileage if applicable
        Vehicle v = req.getVehicle();
        if (dto.getMileageAtService() > v.getCurrentMileage()) {
            v.setPreviousMileage(v.getCurrentMileage());
            v.setCurrentMileage(dto.getMileageAtService());
            vehicleRepository.save(v);
        }

        // 4. Transition state
        req.setServiceRecord(record);
        req.setState(ServiceRequestState.COMPLETED);
        req.setCompletedAt(LocalDateTime.now());
        ServiceRequest saved = serviceRequestRepository.save(req);

        // 5. Fire Observer event
        eventPublisher.publishEvent(new ServiceCompletionEvent(this, record));
        return saved;
    }

    @Override
    public ServiceRequest cancel(Long requestId, String reason) {
        ServiceRequest req = findById(requestId);
        validateNotCompleted(req);
        if (req.getState() == ServiceRequestState.CANCELLED)
            throw new IllegalStateException("Already cancelled.");
        req.setState(ServiceRequestState.CANCELLED);
        req.setCancellationReason(reason);
        Vehicle v = req.getVehicle();
        v.setState(VehicleState.NEEDS_SERVICE);
        vehicleRepository.save(v);
        return serviceRequestRepository.save(req);
    }

    @Override
    public void validateNotCompleted(ServiceRequest req) {
        if (req.getState() == ServiceRequestState.COMPLETED)
            throw new IllegalStateException(
                "Cannot modify COMPLETED request [" + req.getId() + "].");
    }
}