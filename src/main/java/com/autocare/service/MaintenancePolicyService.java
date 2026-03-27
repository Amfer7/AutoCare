package com.autocare.service;

import com.autocare.model.entity.MaintenancePolicy;
import java.util.List;

public interface MaintenancePolicyService {
    MaintenancePolicy create(MaintenancePolicy policy);
    MaintenancePolicy findById(Long id);
    List<MaintenancePolicy> findAllActive();
    MaintenancePolicy update(Long id, MaintenancePolicy updated);
    void deactivate(Long id);
    MaintenancePolicy getDefaultPolicy();
}