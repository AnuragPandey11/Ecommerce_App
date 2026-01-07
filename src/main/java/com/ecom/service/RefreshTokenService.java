package com.ecom.service;

import com.ecom.entity.RefreshToken;
import com.ecom.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken validateAndGet(String token);
    void revokeToken(RefreshToken token);
    void revokeAllUserTokens(Long userId); // new
}
