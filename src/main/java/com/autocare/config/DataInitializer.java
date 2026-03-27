package com.autocare.config;

import com.autocare.model.entity.*;
import com.autocare.model.enums.UserRole;
import com.autocare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default data on first startup (idempotent — safe to re-run).
 * Creates: Standard, Premium, Heavy-Duty policies + default admin account.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MaintenancePolicyRepository policyRepository;
    private final UserRepository              userRepository;
    private final PasswordEncoder             passwordEncoder;

    @Override
    public void run(String... args) {
        seedPolicies();
        seedAdminUser();
    }

    private void seedPolicies() {
        if (policyRepository.findByName("Standard").isEmpty()) {
            policyRepository.save(MaintenancePolicy.builder()
                    .name("Standard")
                    .description("Service every 5000 km or 6 months.")
                    .mileageInterval(5000.0).mileageDueSoonThreshold(500.0)
                    .timeIntervalDays(180).timeDueSoonThresholdDays(14)
                    .active(true).build());
            log.info("Seeded: Standard policy");
        }

        if (policyRepository.findByName("Premium").isEmpty()) {
            policyRepository.save(MaintenancePolicy.builder()
                    .name("Premium")
                    .description("Service every 3000 km or 3 months.")
                    .mileageInterval(3000.0).mileageDueSoonThreshold(300.0)
                    .timeIntervalDays(90).timeDueSoonThresholdDays(10)
                    .active(true).build());
            log.info("Seeded: Premium policy");
        }

        if (policyRepository.findByName("Heavy-Duty").isEmpty()) {
            policyRepository.save(MaintenancePolicy.builder()
                    .name("Heavy-Duty")
                    .description("Service every 10000 km or 12 months.")
                    .mileageInterval(10000.0).mileageDueSoonThreshold(1000.0)
                    .timeIntervalDays(365).timeDueSoonThresholdDays(30)
                    .active(true).build());
            log.info("Seeded: Heavy-Duty policy");
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@autocare.com")) {
            userRepository.save(Admin.builder()
                    .fullName("System Administrator")
                    .email("admin@autocare.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .role(UserRole.ADMIN)
                    .department("Operations")
                    .active(true).build());
            log.info("Seeded: admin@autocare.com / Admin@1234");
        }
    }
}