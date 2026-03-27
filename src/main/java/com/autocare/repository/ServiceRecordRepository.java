package com.autocare.repository;

import com.autocare.model.entity.ServiceRecord;
import com.autocare.model.entity.Vehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.serviceRequest.vehicle = :vehicle ORDER BY sr.recordedAt DESC")
    List<ServiceRecord> findByVehicleOrderByDateDesc(@Param("vehicle") Vehicle vehicle);
}
