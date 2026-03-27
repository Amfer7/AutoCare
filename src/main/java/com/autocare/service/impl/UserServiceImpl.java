package com.autocare.service.impl;

import com.autocare.model.dto.UserRegistrationDto;
import com.autocare.model.entity.*;
import com.autocare.model.enums.UserRole;
import com.autocare.repository.*;
import com.autocare.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository         userRepository;
    private final VehicleOwnerRepository ownerRepository;
    private final ServiceStaffRepository staffRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    public User register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException(
                "Account with email [" + dto.getEmail() + "] already exists.");
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match.");

        String enc = passwordEncoder.encode(dto.getPassword());

        User user = switch (dto.getRole()) {
            case OWNER -> VehicleOwner.builder()
                    .fullName(dto.getFullName()).email(dto.getEmail())
                    .password(enc).phone(dto.getPhone())
                    .role(UserRole.OWNER).active(true).build();
            case STAFF -> ServiceStaff.builder()
                    .fullName(dto.getFullName()).email(dto.getEmail())
                    .password(enc).phone(dto.getPhone())
                    .role(UserRole.STAFF).specialization(dto.getSpecialization())
                    .active(true).build();
            case ADMIN -> Admin.builder()
                    .fullName(dto.getFullName()).email(dto.getEmail())
                    .password(enc).phone(dto.getPhone())
                    .role(UserRole.ADMIN).department(dto.getDepartment())
                    .active(true).build();
        };

        return userRepository.save(user);
    }

    @Override @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    @Override @Transactional(readOnly = true)
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Override @Transactional(readOnly = true)
    public List<User> findAllByRole(UserRole role) { return userRepository.findByRole(role); }

    @Override @Transactional(readOnly = true)
    public List<User> findAllActive() { return userRepository.findByActiveTrue(); }

    @Override
    public User toggleActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    @Override @Transactional(readOnly = true)
    public VehicleOwner getOwnerById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public ServiceStaff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public boolean emailExists(String email) { return userRepository.existsByEmail(email); }
}