package com.autocare.repository;

import com.autocare.model.entity.MaintenancePolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenancePolicyRepository extends JpaRepository<MaintenancePolicy, Long> {
    List<MaintenancePolicy> findByActiveTrue();

    Optional<MaintenancePolicy> findByName(String name);
}
