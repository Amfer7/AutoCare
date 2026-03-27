package com.autocare.repository;

import com.autocare.model.entity.Vehicle;
import com.autocare.model.entity.VehicleOwner;
import com.autocare.model.enums.VehicleState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwner(VehicleOwner owner);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByState(VehicleState state);
}
