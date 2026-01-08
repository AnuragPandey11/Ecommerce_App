package com.ecom.service;


import com.ecom.dto.ChangePasswordRequest;
import com.ecom.dto.ProfileUpdateRequest;
import com.ecom.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser(String email);
    UserResponse updateProfile(String email, ProfileUpdateRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUserRoles(Long userId, List<String> roles);
    void deleteUser(Long userId);
}
