package com.autocare.controller;

import com.autocare.config.AuthenticationHelper;
import com.autocare.model.entity.*;
import com.autocare.model.enums.*;
import com.autocare.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AuthenticationHelper  authHelper;
    private final VehicleService        vehicleService;
    private final ReminderService       reminderService;
    private final ServiceRequestService serviceRequestService;
    private final BillingService        billingService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerDashboard(Model model) {
        VehicleOwner owner   = authHelper.getCurrentOwner();
        var vehicles         = vehicleService.findByOwner(owner);
        var reminders        = reminderService.findAllByOwner(owner.getId());
        var requests         = serviceRequestService.findByOwner(owner);
        var bills            = billingService.findByOwner(owner.getId());

        model.addAttribute("owner",        owner);
        model.addAttribute("vehicles",     vehicles);
        model.addAttribute("reminders",    reminders);
        model.addAttribute("requests",     requests);
        model.addAttribute("bills",        bills.stream().map(billingService::toBillDto).toList());
        model.addAttribute("vehicleCount", vehicles.size());
        model.addAttribute("overdueCount", reminders.stream()
                .filter(r -> r.getState() == ReminderState.OVERDUE).count());
        model.addAttribute("dueSoonCount", reminders.stream()
                .filter(r -> r.getState() == ReminderState.DUE_SOON).count());
        model.addAttribute("pendingCount", requests.stream()
                .filter(r -> r.getState() == ServiceRequestState.PENDING).count());
        model.addAttribute("unpaidCount",  bills.stream().filter(b -> !b.isPaid()).count());
        return "dashboard/owner";
    }

    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard(Model model) {
        ServiceStaff staff = authHelper.getCurrentStaff();
        var assigned       = serviceRequestService.findByStaff(staff);
        var pending        = serviceRequestService.findByState(ServiceRequestState.PENDING);
        var inProgress     = serviceRequestService.findByState(ServiceRequestState.IN_PROGRESS);

        model.addAttribute("staff",            staff);
        model.addAttribute("assignedRequests", assigned);
        model.addAttribute("pendingRequests",  pending);
        model.addAttribute("inProgressCount",  inProgress.size());
        model.addAttribute("completedCount",   assigned.stream()
                .filter(r -> r.getState() == ServiceRequestState.COMPLETED).count());
        return "dashboard/staff";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        var allRequests  = serviceRequestService.findAll();
        var needsService = vehicleService.findByState(VehicleState.NEEDS_SERVICE);
        var underService = vehicleService.findByState(VehicleState.UNDER_SERVICE);
        var unpaidBills  = billingService.findUnpaid();

        model.addAttribute("admin",          authHelper.getCurrentUser());
        model.addAttribute("totalRequests",  allRequests.size());
        model.addAttribute("needsService",   needsService.size());
        model.addAttribute("underService",   underService.size());
        model.addAttribute("unpaidBills",    unpaidBills.stream()
                .map(billingService::toBillDto).toList());
        model.addAttribute("recentRequests", allRequests.stream().limit(10).toList());
        return "dashboard/admin";
    }

    @GetMapping("/")
    public String root() { return "redirect:/auth/login"; }
}