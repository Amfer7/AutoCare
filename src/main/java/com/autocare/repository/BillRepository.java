package com.autocare.repository;

import com.autocare.model.entity.Bill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByServiceRecordId(Long serviceRecordId);

    @Query("SELECT b FROM Bill b WHERE b.serviceRecord.serviceRequest.owner.id = :ownerId ORDER BY b.generatedAt DESC")
    List<Bill> findAllByOwnerId(@Param("ownerId") Long ownerId);

    List<Bill> findByPaidFalseOrderByGeneratedAtDesc();
}
