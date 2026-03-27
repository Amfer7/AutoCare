package com.autocare.controller;

import com.autocare.model.entity.MaintenancePolicy;
import com.autocare.model.enums.UserRole;
import com.autocare.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService              userService;
    private final MaintenancePolicyService policyService;
    private final VehicleService           vehicleService;
    private final ServiceRequestService    serviceRequestService;
    private final BillingService           billingService;

    @GetMapping("/users")
    public String manageUsers(@RequestParam(required = false) String role, Model model) {
        var users = (role != null && !role.isBlank())
                ? userService.findAllByRole(UserRole.valueOf(role.toUpperCase()))
                : userService.findAllActive();
        model.addAttribute("users",      users);
        model.addAttribute("filter",     role);
        model.addAttribute("roles",      UserRole.values());
        model.addAttribute("ownerCount", userService.findAllByRole(UserRole.OWNER).size());
        model.addAttribute("staffCount", userService.findAllByRole(UserRole.STAFF).size());
        model.addAttribute("adminCount", userService.findAllByRole(UserRole.ADMIN).size());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            var user = userService.toggleActive(id);
            redirectAttrs.addFlashAttribute("successMsg",
                user.getFullName() + " is now " + (user.isActive() ? "active" : "deactivated") + ".");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/admin/users";
    }

    @GetMapping("/policies")
    public String managePolicies(Model model) {
        model.addAttribute("policies",  policyService.findAllActive());
        model.addAttribute("newPolicy", new MaintenancePolicy());
        return "admin/policies";
    }

    @PostMapping("/policies/create")
    public String createPolicy(@ModelAttribute MaintenancePolicy policy,
            RedirectAttributes redirectAttrs) {
        try { policyService.create(policy);
              redirectAttrs.addFlashAttribute("successMsg",
                  "Policy '" + policy.getName() + "' created.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/admin/policies";
    }

    @PostMapping("/policies/{id}/deactivate")
    public String deactivatePolicy(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try { policyService.deactivate(id);
              redirectAttrs.addFlashAttribute("successMsg", "Policy deactivated.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/admin/policies";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        var allRequests = serviceRequestService.findAll();
        var unpaidBills = billingService.findUnpaid();
        double totalRevenue = allRequests.stream()
                .filter(r -> r.getServiceRecord() != null
                          && r.getServiceRecord().getBill() != null)
                .mapToDouble(r -> r.getServiceRecord().getBill().getTotalAmount()).sum();
        model.addAttribute("totalRequests",     allRequests.size());
        model.addAttribute("completedRequests", allRequests.stream()
                .filter(r -> r.getState() ==
                    com.autocare.model.enums.ServiceRequestState.COMPLETED).count());
        model.addAttribute("totalRevenue",      totalRevenue);
        model.addAttribute("outstanding",       unpaidBills.stream()
                .mapToDouble(b -> b.getTotalAmount()).sum());
        model.addAttribute("recentRequests",    allRequests.stream().limit(20).toList());
        model.addAttribute("unpaidBills",       unpaidBills.stream()
                .map(billingService::toBillDto).toList());
        return "admin/reports";
    }
}