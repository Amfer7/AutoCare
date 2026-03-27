package com.autocare.repository;

import com.autocare.model.entity.Reminder;
import com.autocare.model.entity.Vehicle;
import com.autocare.model.enums.ReminderState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByVehicle(Vehicle vehicle);

    List<Reminder> findByVehicleAndState(Vehicle vehicle, ReminderState state);

    @Query("SELECT r FROM Reminder r WHERE r.vehicle = :vehicle AND r.state != 'COMPLETED' ORDER BY r.createdAt DESC")
    Optional<Reminder> findActiveReminderForVehicle(@Param("vehicle") Vehicle vehicle);

    @Query("SELECT r FROM Reminder r WHERE r.state != 'COMPLETED'")
    List<Reminder> findAllActiveReminders();

    @Query("SELECT r FROM Reminder r WHERE r.vehicle.owner.id = :ownerId ORDER BY r.state, r.dueDate")
    List<Reminder> findAllByOwnerId(@Param("ownerId") Long ownerId);
}
