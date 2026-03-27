package com.autocare.service.impl;

import com.autocare.model.dto.BillDto;
import com.autocare.model.entity.*;
import com.autocare.repository.BillRepository;
import com.autocare.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;

    @Value("${app.billing.tax-rate:18.0}")
    private double defaultTaxRate;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Override
    public Bill generateBill(ServiceRecord record, double laborCost,
                             double partsCost, String notes) {
        return billRepository.save(Bill.builder()
                .serviceRecord(record).laborCost(laborCost)
                .partsCost(partsCost).taxRate(defaultTaxRate)
                .notes(notes).paid(false).build());
    }

    @Override @Transactional(readOnly = true)
    public Bill findById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Bill findByServiceRecord(ServiceRecord record) {
        return billRepository.findByServiceRecordId(record.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Bill not found for record: " + record.getId()));
    }

    @Override @Transactional(readOnly = true)
    public List<Bill> findByOwner(Long ownerId) {
        return billRepository.findAllByOwnerId(ownerId);
    }

    @Override @Transactional(readOnly = true)
    public List<Bill> findUnpaid() {
        return billRepository.findByPaidFalseOrderByGeneratedAtDesc();
    }

    @Override
    public Bill markPaid(Long billId) {
        Bill bill = findById(billId);
        if (bill.isPaid()) throw new IllegalStateException(
            "Bill [" + billId + "] is already paid.");
        bill.setPaid(true);
        bill.setPaidAt(LocalDateTime.now());
        return billRepository.save(bill);
    }

    @Override @Transactional(readOnly = true)
    public BillDto toBillDto(Bill bill) {
        var record  = bill.getServiceRecord();
        var request = record.getServiceRequest();
        var vehicle = request.getVehicle();
        return BillDto.builder()
                .billId(bill.getId())
                .serviceRequestId(request.getId())
                .vehicleDisplayName(vehicle.getDisplayName()
                        + " (" + vehicle.getLicensePlate() + ")")
                .ownerName(request.getOwner().getFullName())
                .staffName(request.getAssignedStaff() != null
                        ? request.getAssignedStaff().getFullName() : "Unassigned")
                .laborCost(bill.getLaborCost()).partsCost(bill.getPartsCost())
                .taxRate(bill.getTaxRate()).subtotal(bill.getSubtotal())
                .taxAmount(bill.getTaxAmount()).totalAmount(bill.getTotalAmount())
                .paid(bill.isPaid())
                .generatedAt(bill.getGeneratedAt() != null
                        ? bill.getGeneratedAt().format(FMT) : "—")
                .paidAt(bill.getPaidAt() != null
                        ? bill.getPaidAt().format(FMT) : "—")
                .notes(bill.getNotes()).build();
    }
}