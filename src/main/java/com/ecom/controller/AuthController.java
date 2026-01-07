package com.ecom.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.ApiResponse;
import com.ecom.dto.JwtAuthResponse;
import com.ecom.dto.LoginRequest;
import com.ecom.dto.RefreshTokenRequest;
import com.ecom.dto.RefreshTokenResponse;
import com.ecom.dto.RegisterRequest;
import com.ecom.dto.UserResponse;
import com.ecom.entity.AuditLog;
import com.ecom.entity.RefreshToken;
import com.ecom.entity.Role;
import com.ecom.entity.User;
import com.ecom.exception.BadRequestException;
import com.ecom.exception.EmailAlreadyExistsException;
import com.ecom.repository.RoleRepository;
import com.ecom.repository.UserRepository;
import com.ecom.security.CurrentUser;
import com.ecom.security.JwtTokenProvider;
import com.ecom.security.LoginAttemptService;
import com.ecom.security.UserPrincipal;
import com.ecom.service.AuditService;
import com.ecom.service.MailService;
import com.ecom.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);
        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final LoginAttemptService loginAttemptService;
        private final RefreshTokenService refreshTokenService;
        private final MailService mailService;

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<UserResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new EmailAlreadyExistsException("Email already registered");
                }

                Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                                .orElseThrow(() -> new IllegalStateException("ROLE_USER not configured"));

                String verificationToken = java.util.UUID.randomUUID().toString();

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .isVerified(false)
                                .oauthProvider(User.OAuthProvider.LOCAL)
                                .verificationToken(verificationToken)
                                .build();
                user.getRoles().add(userRole);

                User saved = userRepository.save(user);

                mailService.sendVerificationEmail(saved.getEmail(), verificationToken);

                UserResponse response = mapToUserResponse(saved);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("User registered successfully. Please verify your email.",
                                                response));
        }

        @GetMapping("/verify-email")
        public ResponseEntity<ApiResponse<String>> verifyEmail(
                        @RequestParam("token") String token) {
                User user = userRepository.findByVerificationToken(token)
                                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

                user.setIsVerified(true);
                user.setVerificationToken(null);
                userRepository.save(user);

                return ResponseEntity.ok(
                                ApiResponse.success("Email verified successfully", "verified"));
        }


      
        private final AuditService auditService;

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest servletRequest) {
                String ip = servletRequest.getRemoteAddr();
                User user = null;

                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(), request.getPassword()));
                        loginAttemptService.loginSucceeded(ip, request.getEmail());

                        user = userRepository.findByEmail(request.getEmail()).orElseThrow();

                        // Log successful login
                        auditService.logEvent(user, AuditLog.EventType.LOGIN_SUCCESS, true,
                                        "User logged in", servletRequest);

                        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                        JwtAuthResponse payload = JwtAuthResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken.getToken())
                                        .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                                        .user(mapToUserResponse(user))
                                        .build();

                        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", payload));

                } catch (BadCredentialsException ex) {
                        loginAttemptService.loginFailed(ip, request.getEmail());

                        // Log failed login
                        if (user == null) {
                                user = userRepository.findByEmail(request.getEmail()).orElse(null);
                        }
                        auditService.logEvent(user, AuditLog.EventType.LOGIN_FAILED, false,
                                        "Invalid credentials", servletRequest);

                        throw ex;
                }
        }

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request,
                        HttpServletRequest servletRequest) {
                RefreshToken existing = refreshTokenService.validateAndGet(request.getRefreshToken());
                User user = existing.getUser();

                // Log token refresh
                auditService.logEvent(user, AuditLog.EventType.TOKEN_REFRESH, true,
                                "Access token refreshed", servletRequest);

                String accessToken = jwtTokenProvider.generateTokenFromUser(user);

                RefreshTokenResponse response = RefreshTokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(existing.getToken())
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                                .build();

                return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        }

        private UserResponse mapToUserResponse(User user) {
                return UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .isVerified(user.getIsVerified())
                                .roles(user.getRoles().stream()
                                                .map(r -> r.getRoleName().name())
                                                .collect(java.util.stream.Collectors.toSet()))
                                .build();
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<String>> logout(
                        @CurrentUser UserPrincipal principal,
                        @Valid @RequestBody RefreshTokenRequest request,
                        HttpServletRequest servletRequest) {
                User user = userRepository.findById(principal.getId()).orElseThrow();

                try {
                        RefreshToken token = refreshTokenService.validateAndGet(request.getRefreshToken());
                        refreshTokenService.revokeToken(token);

                        auditService.logEvent(user, AuditLog.EventType.LOGOUT, true,
                                        "User logged out", servletRequest);
                } catch (Exception e) {
                        auditService.logEvent(user, AuditLog.EventType.LOGOUT, false,
                                        e.getMessage(), servletRequest);
                }

                return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "logged_out"));
        }

}
