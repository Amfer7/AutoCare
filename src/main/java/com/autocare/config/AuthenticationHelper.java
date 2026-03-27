package com.autocare.config;

import com.autocare.model.entity.*;
import com.autocare.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated user into a typed entity.
 * Used by controllers to avoid duplicating security lookup logic. (SRP)
 */
@Component
@RequiredArgsConstructor
public class AuthenticationHelper {

    private final UserRepository         userRepository;
    private final VehicleOwnerRepository ownerRepository;
    private final ServiceStaffRepository staffRepository;

    public String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public User getCurrentUser() {
        return userRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new IllegalStateException(
                    "Authenticated user not found in DB"));
    }

    public VehicleOwner getCurrentOwner() {
        return ownerRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new IllegalStateException(
                    "Current user is not a VehicleOwner"));
    }

    public ServiceStaff getCurrentStaff() {
        return staffRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new IllegalStateException(
                    "Current user is not ServiceStaff"));
    }
}