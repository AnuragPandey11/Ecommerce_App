package com.ecom.service.impl;


import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.entity.RefreshToken;
import com.ecom.entity.User;
import com.ecom.exception.UnauthorizedException;
import com.ecom.repository.RefreshTokenRepository;
import com.ecom.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Override
    public RefreshToken createRefreshToken(User user) {
        // option: rotate by revoking existing tokens for this user
        Instant expiry = Instant.now().plusMillis(refreshTokenExpirationMs);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(java.util.UUID.randomUUID().toString())
                .expiryDate(expiry)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    @Override
    public RefreshToken validateAndGet(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (rt.getRevoked() || rt.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }
        return rt;
    }

    @Override
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Override
public void revokeAllUserTokens(Long userId) {
    List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(userId);
    userTokens.forEach(token -> token.setRevoked(true));
    refreshTokenRepository.saveAll(userTokens);
}

}
