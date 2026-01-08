package com.ecom.config;

import com.ecom.entity.Role;
import com.ecom.entity.User;
import com.ecom.repository.RoleRepository;
import com.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "default"})
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Running DataInitializer...");

        // ✅ FIXED: Create roles using String name instead of enum
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("ROLE_ADMIN")
                            .description("Administrator role with full access")
                            .build();
                    return roleRepository.save(role);
                });

        Role staffRole = roleRepository.findByName("ROLE_STAFF")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("ROLE_STAFF")
                            .description("Staff role with limited administrative access")
                            .build();
                    return roleRepository.save(role);
                });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("ROLE_USER")
                            .description("Default user role")
                            .build();
                    return roleRepository.save(role);
                });

        log.info("Roles initialized: ROLE_ADMIN, ROLE_STAFF, ROLE_USER");

        // Create default admin user
        String adminEmail = "admin@ecom.local";
        if (!userRepository.existsByEmail(adminEmail)) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(staffRole);
            adminRoles.add(userRole);

            User admin = User.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@12345"))
                    .isVerified(true)
                    .roles(adminRoles)
                    .build();
            
            userRepository.save(admin);
            log.info("✅ Created default admin user with email: {}", adminEmail);
            log.info("   Username: {}", adminEmail);
            log.info("   Password: Admin@12345");
        } else {
            log.info("ℹ️  Admin user already exists, skipping creation");
        }
    }
}
