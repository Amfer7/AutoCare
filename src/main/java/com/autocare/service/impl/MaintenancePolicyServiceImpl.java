package com.autocare.service.impl;

import com.autocare.model.entity.MaintenancePolicy;
import com.autocare.repository.MaintenancePolicyRepository;
import com.autocare.service.MaintenancePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenancePolicyServiceImpl implements MaintenancePolicyService {

    private final MaintenancePolicyRepository policyRepository;

    @Override
    public MaintenancePolicy create(MaintenancePolicy policy) {
        if (policyRepository.findByName(policy.getName()).isPresent())
            throw new IllegalArgumentException(
                "Policy [" + policy.getName() + "] already exists.");
        return policyRepository.save(policy);
    }

    @Override @Transactional(readOnly = true)
    public MaintenancePolicy findById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public List<MaintenancePolicy> findAllActive() { return policyRepository.findByActiveTrue(); }

    @Override
    public MaintenancePolicy update(Long id, MaintenancePolicy updated) {
        MaintenancePolicy p = findById(id);
        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setMileageInterval(updated.getMileageInterval());
        p.setMileageDueSoonThreshold(updated.getMileageDueSoonThreshold());
        p.setTimeIntervalDays(updated.getTimeIntervalDays());
        p.setTimeDueSoonThresholdDays(updated.getTimeDueSoonThresholdDays());
        return policyRepository.save(p);
    }

    @Override
    public void deactivate(Long id) {
        MaintenancePolicy p = findById(id);
        p.setActive(false);
        policyRepository.save(p);
    }

    @Override @Transactional(readOnly = true)
    public MaintenancePolicy getDefaultPolicy() {
        return policyRepository.findByName("Standard")
                .orElseThrow(() -> new IllegalStateException(
                    "Default 'Standard' policy not found. Run data initializer."));
    }
}