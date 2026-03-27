package com.autocare.controller;

import com.autocare.model.dto.UserRegistrationDto;
import com.autocare.model.enums.UserRole;
import com.autocare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null)  model.addAttribute("errorMsg",  "Invalid email or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("dto",   new UserRegistrationDto());
        model.addAttribute("roles", UserRole.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("dto") UserRegistrationDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            result.rejectValue("confirmPassword", "error.dto", "Passwords do not match.");
        if (userService.emailExists(dto.getEmail()))
            result.rejectValue("email", "error.dto",
                "An account with this email already exists.");

        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttrs.addFlashAttribute("successMsg",
                "Account created. Please log in.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }
    }
}