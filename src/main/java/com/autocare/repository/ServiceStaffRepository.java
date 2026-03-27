package com.autocare.repository;

import com.autocare.model.entity.ServiceStaff;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceStaffRepository extends JpaRepository<ServiceStaff, Long> {
    Optional<ServiceStaff> findByEmail(String email);
    List<ServiceStaff> findByActiveTrueOrderByFullName();
}
