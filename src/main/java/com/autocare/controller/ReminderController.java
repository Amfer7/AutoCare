package com.autocare.controller;

import com.autocare.config.AuthenticationHelper;
import com.autocare.model.enums.ReminderState;
import com.autocare.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService      reminderService;
    private final AuthenticationHelper authHelper;

    @GetMapping("/list")
    @PreAuthorize("hasRole('OWNER')")
    public String listReminders(
            @RequestParam(required = false) String filter, Model model) {
        var owner = authHelper.getCurrentOwner();
        var all   = reminderService.findAllByOwner(owner.getId());
        var filtered = (filter != null && !filter.isBlank())
                ? all.stream().filter(r -> r.getState().name().equalsIgnoreCase(filter)).toList()
                : all;

        model.addAttribute("reminders",    filtered);
        model.addAttribute("filter",       filter);
        model.addAttribute("overdueCount", all.stream().filter(r -> r.getState() == ReminderState.OVERDUE).count());
        model.addAttribute("dueSoonCount", all.stream().filter(r -> r.getState() == ReminderState.DUE_SOON).count());
        model.addAttribute("upcomingCount",all.stream().filter(r -> r.getState() == ReminderState.UPCOMING).count());
        return "reminder/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String reminderDetail(@PathVariable Long id, Model model) {
        var owner   = authHelper.getCurrentOwner();
        var reminder = reminderService.findAllByOwner(owner.getId()).stream()
                .filter(r -> r.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + id));
        model.addAttribute("reminder", reminder);
        model.addAttribute("vehicle",  reminder.getVehicle());
        model.addAttribute("policy",   reminder.getPolicy());
        return "reminder/detail";
    }
}