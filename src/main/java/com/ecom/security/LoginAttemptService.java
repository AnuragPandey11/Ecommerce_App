package com.ecom.security;


import com.ecom.entity.LoginAttempt;
import com.ecom.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${app.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    public void loginFailed(String ipAddress, String email) {
        LoginAttempt attempt = loginAttemptRepository
                .findByIpAddress(ipAddress)
                .orElse(
                        LoginAttempt.builder()
                                .ipAddress(ipAddress)
                                .email(email)
                                .attempts(0)
                                .build()
                );
        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setLastModified(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }

    public void loginSucceeded(String ipAddress, String email) {
        loginAttemptRepository.findByIpAddress(ipAddress).ifPresent(loginAttemptRepository::delete);
    }

    public boolean isBlocked(String ipAddress) {
        return loginAttemptRepository.findByIpAddress(ipAddress)
                .map(attempt -> {
                    if (attempt.getAttempts() >= maxLoginAttempts) {
                        LocalDateTime unlockTime = attempt.getLastModified()
                                .plusMinutes(lockoutDurationMinutes);
                        return LocalDateTime.now().isBefore(unlockTime);
                    }
                    return false;
                })
                .orElse(false);
    }
}
