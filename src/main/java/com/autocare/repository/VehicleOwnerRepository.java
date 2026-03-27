package com.autocare.repository;

import com.autocare.model.entity.VehicleOwner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleOwnerRepository extends JpaRepository<VehicleOwner, Long> {
    Optional<VehicleOwner> findByEmail(String email);
}
