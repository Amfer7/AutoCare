package com.autocare.controller;

import com.autocare.config.AuthenticationHelper;
import com.autocare.model.dto.*;
import com.autocare.model.entity.VehicleOwner;
import com.autocare.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService          vehicleService;
    private final MaintenancePolicyService policyService;
    private final ReminderService         reminderService;
    private final AuthenticationHelper    authHelper;

    @GetMapping("/list")
    @PreAuthorize("hasRole('OWNER')")
    public String listVehicles(Model model) {
        VehicleOwner owner = authHelper.getCurrentOwner();
        var vehicles = vehicleService.findByOwner(owner);
        var vehicleReminders = vehicles.stream().collect(Collectors.toMap(
                v -> v.getId(),
                v -> reminderService.findActiveReminder(v).orElse(null),
                (a, b) -> a, LinkedHashMap::new));
        model.addAttribute("vehicles",         vehicles);
        model.addAttribute("vehicleReminders", vehicleReminders);
        return "vehicle/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('OWNER')")
    public String addVehiclePage(Model model) {
        model.addAttribute("dto",      new VehicleRegistrationDto());
        model.addAttribute("policies", policyService.findAllActive());
        return "vehicle/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('OWNER')")
    public String addVehicle(
            @Valid @ModelAttribute("dto") VehicleRegistrationDto dto,
            BindingResult result, Model model, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("policies", policyService.findAllActive());
            return "vehicle/add";
        }
        try {
            var vehicle = vehicleService.register(dto, authHelper.getCurrentOwner());
            redirectAttrs.addFlashAttribute("successMsg",
                vehicle.getDisplayName() + " registered successfully!");
            return "redirect:/vehicle/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("policies", policyService.findAllActive());
            return "vehicle/add";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String vehicleDetail(@PathVariable Long id, Model model) {
        var vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle",        vehicle);
        model.addAttribute("reminders",      reminderService.findByVehicle(vehicle));
        model.addAttribute("activeReminder", reminderService.findActiveReminder(vehicle).orElse(null));
        model.addAttribute("mileageDto",     new MileageUpdateDto());
        return "vehicle/detail";
    }

    @PostMapping("/{id}/mileage")
    @PreAuthorize("hasAnyRole('OWNER','STAFF')")
    public String updateMileage(
            @PathVariable Long id,
            @Valid @ModelAttribute("mileageDto") MileageUpdateDto dto,
            BindingResult result, Model model, RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            var vehicle = vehicleService.findById(id);
            model.addAttribute("vehicle",        vehicle);
            model.addAttribute("reminders",      reminderService.findByVehicle(vehicle));
            model.addAttribute("activeReminder", reminderService.findActiveReminder(vehicle).orElse(null));
            return "vehicle/detail";
        }
        try {
            vehicleService.updateMileage(id, dto, authHelper.getCurrentEmail());
            redirectAttrs.addFlashAttribute("successMsg",
                "Mileage updated to " + dto.getNewMileage() + " km.");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/vehicle/" + id;
    }
}