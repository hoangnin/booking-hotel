package com.lenin.hotel.authentication.repository;

import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.token = :token, rt.expiryDate = :expiryDate WHERE rt.user.id = :userId")
    void updateToken(@Param("token") String token, @Param("expiryDate") Instant expiryDate, @Param("userId") Long userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);

    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
    @Modifying
    int deleteByUser(User user);
}
