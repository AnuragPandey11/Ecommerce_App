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

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "default"}) // adjust if you also want this in prod
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Running DataInitializer...");

        Role adminRole = roleRepository.findByRoleName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName(Role.RoleName.ROLE_ADMIN).build()
                ));

        Role staffRole = roleRepository.findByRoleName(Role.RoleName.ROLE_STAFF)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName(Role.RoleName.ROLE_STAFF).build()
                ));

        Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().roleName(Role.RoleName.ROLE_USER).build()
                ));

        String adminEmail = "admin@ecom.local";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@12345"))
                    .isVerified(true)
                    .oauthProvider(User.OAuthProvider.LOCAL)
                    .roles(Set.of(adminRole, staffRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user with email: {}", adminEmail);
        } else {
            log.info("Admin user already exists, skipping creation");
        }
    }
}

