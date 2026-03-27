package com.autocare.repository;

import com.autocare.model.entity.ServiceRequest;
import com.autocare.model.entity.ServiceStaff;
import com.autocare.model.entity.VehicleOwner;
import com.autocare.model.enums.ServiceRequestState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByOwnerOrderByRequestedAtDesc(VehicleOwner owner);

    List<ServiceRequest> findByAssignedStaffOrderByRequestedAtDesc(ServiceStaff staff);

    List<ServiceRequest> findByStateOrderByRequestedAtDesc(ServiceRequestState state);
}
