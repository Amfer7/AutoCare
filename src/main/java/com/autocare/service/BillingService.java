package com.autocare.service;

import com.autocare.model.dto.BillDto;
import com.autocare.model.entity.*;
import java.util.List;

public interface BillingService {
    Bill generateBill(ServiceRecord record, double laborCost,
                      double partsCost, String notes);
    Bill findById(Long id);
    Bill findByServiceRecord(ServiceRecord record);
    List<Bill> findByOwner(Long ownerId);
    List<Bill> findUnpaid();
    Bill markPaid(Long billId);
    BillDto toBillDto(Bill bill);
}