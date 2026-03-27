package com.autocare.controller;

import com.autocare.config.AuthenticationHelper;
import com.autocare.model.dto.*;
import com.autocare.model.entity.*;
import com.autocare.model.enums.*;
import com.autocare.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final VehicleService        vehicleService;
    private final UserService           userService;
    private final AuthenticationHelper  authHelper;

    @GetMapping("/list")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerRequestList(Model model) {
        VehicleOwner owner = authHelper.getCurrentOwner();
        var requests = serviceRequestService.findByOwner(owner);
        model.addAttribute("requests",        requests);
        model.addAttribute("pendingCount",    requests.stream().filter(r -> r.getState() == ServiceRequestState.PENDING).count());
        model.addAttribute("inProgressCount", requests.stream().filter(r -> r.getState() == ServiceRequestState.IN_PROGRESS).count());
        return "service/request-list";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String allRequestList(@RequestParam(required = false) String state, Model model) {
        var requests  = (state != null && !state.isBlank())
                ? serviceRequestService.findByState(ServiceRequestState.valueOf(state.toUpperCase()))
                : serviceRequestService.findAll();
        model.addAttribute("requests",  requests);
        model.addAttribute("staffList", userService.findAllByRole(UserRole.STAFF));
        model.addAttribute("filter",    state);
        model.addAttribute("states",    ServiceRequestState.values());
        return "service/all-requests";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('OWNER')")
    public String createRequestPage(Model model) {
        VehicleOwner owner = authHelper.getCurrentOwner();
        model.addAttribute("dto",      new ServiceRequestDto());
        model.addAttribute("vehicles", vehicleService.findByOwner(owner));
        return "service/create-request";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('OWNER')")
    public String createRequest(
            @Valid @ModelAttribute("dto") ServiceRequestDto dto,
            BindingResult result, Model model, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            VehicleOwner owner = authHelper.getCurrentOwner();
            model.addAttribute("vehicles", vehicleService.findByOwner(owner));
            return "service/create-request";
        }
        try {
            var req = serviceRequestService.create(dto, authHelper.getCurrentOwner());
            redirectAttrs.addFlashAttribute("successMsg",
                "Service request #" + req.getId() + " submitted.");
            return "redirect:/service/list";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("vehicles", vehicleService.findByOwner(authHelper.getCurrentOwner()));
            return "service/create-request";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String requestDetail(@PathVariable Long id, Model model) {
        model.addAttribute("request",       serviceRequestService.findById(id));
        model.addAttribute("staffList",     userService.findAllByRole(UserRole.STAFF));
        model.addAttribute("completionDto", new ServiceCompletionDto());
        return "service/update-request";
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String assignStaff(@PathVariable Long id, @RequestParam Long staffId,
            RedirectAttributes redirectAttrs) {
        try { serviceRequestService.assignStaff(id, staffId);
              redirectAttrs.addFlashAttribute("successMsg", "Staff assigned.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/service/" + id;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('STAFF')")
    public String startService(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try { serviceRequestService.startService(id);
              redirectAttrs.addFlashAttribute("successMsg", "Service started.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/service/" + id;
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STAFF')")
    public String completeService(
            @PathVariable Long id,
            @Valid @ModelAttribute("completionDto") ServiceCompletionDto dto,
            BindingResult result, Model model, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("request",   serviceRequestService.findById(id));
            model.addAttribute("staffList", userService.findAllByRole(UserRole.STAFF));
            return "service/update-request";
        }
        try {
            serviceRequestService.completeService(id, dto);
            redirectAttrs.addFlashAttribute("successMsg", "Service completed. Bill generated.");
            return "redirect:/staff/dashboard";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/service/" + id;
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String cancelRequest(@PathVariable Long id,
            @RequestParam(required = false) String reason, RedirectAttributes redirectAttrs) {
        try { serviceRequestService.cancel(id, reason != null ? reason : "No reason provided");
              redirectAttrs.addFlashAttribute("successMsg", "Request cancelled.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/service/list";
    }
}