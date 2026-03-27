package com.autocare.service;

import com.autocare.model.dto.UserRegistrationDto;
import com.autocare.model.entity.*;
import com.autocare.model.enums.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(UserRegistrationDto dto);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAllByRole(UserRole role);
    List<User> findAllActive();
    User toggleActive(Long userId);
    VehicleOwner getOwnerById(Long id);
    ServiceStaff getStaffById(Long id);
    boolean emailExists(String email);
}