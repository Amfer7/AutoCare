package com.autocare.controller;

import com.autocare.config.AuthenticationHelper;
import com.autocare.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillingService       billingService;
    private final AuthenticationHelper authHelper;

    @GetMapping("/list")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerBillList(Model model) {
        var owner = authHelper.getCurrentOwner();
        var bills = billingService.findByOwner(owner.getId())
                .stream().map(billingService::toBillDto).toList();
        model.addAttribute("bills",       bills);
        model.addAttribute("totalUnpaid", bills.stream()
                .filter(b -> !b.isPaid()).mapToDouble(b -> b.getTotalAmount()).sum());
        return "bill/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String viewBill(@PathVariable Long id, Model model) {
        var bill = billingService.findById(id);
        model.addAttribute("bill",    bill);
        model.addAttribute("billDto", billingService.toBillDto(bill));
        return "bill/view";
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('OWNER','STAFF','ADMIN')")
    public String markPaid(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try { billingService.markPaid(id);
              redirectAttrs.addFlashAttribute("successMsg", "Bill marked as paid.");
        } catch (Exception e) { redirectAttrs.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/bill/" + id;
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String unpaidBills(Model model) {
        var bills = billingService.findUnpaid().stream().map(billingService::toBillDto).toList();
        model.addAttribute("bills",            bills);
        model.addAttribute("totalOutstanding", bills.stream()
                .mapToDouble(b -> b.getTotalAmount()).sum());
        return "bill/unpaid";
    }
}