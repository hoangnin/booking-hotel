package com.lenin.hotel.authentication.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.lenin.hotel.common.exception.TokenRefreshException;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.repository.RefreshTokenRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RefreshTokenService {
    @Value("${hotel.jwt.refreshToken}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

//    public RefreshToken createRefreshToken(Long userId) {
//        RefreshToken refreshToken = new RefreshToken();
//
//        refreshToken.setUser(userRepository.findById(userId).get());
//        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
//        refreshToken.setToken(UUID.randomUUID().toString());
//
//        refreshToken = refreshTokenRepository.save(refreshToken);
//        return refreshToken;
//    }

    public RefreshToken createRefreshToken(Long userId) {
        // Xóa refresh token cũ nếu có
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.flush();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
