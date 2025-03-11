package com.lenin.hotel.authentication.controller;

import com.lenin.hotel.common.exception.TokenRefreshException;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.request.LoginRequest;
import com.lenin.hotel.authentication.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.request.SignupRequest;
import com.lenin.hotel.authentication.request.TokenRefreshRequest;
import com.lenin.hotel.authentication.response.MessageResponse;
import com.lenin.hotel.authentication.response.TokenRefreshResponse;
import com.lenin.hotel.authentication.security.JwtUtil;
import com.lenin.hotel.authentication.security.RefreshTokenService;
import com.lenin.hotel.authentication.security.UserDetailsImpl;
import com.lenin.hotel.authentication.service.impl.UserServiceImpl;
import com.lenin.hotel.common.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    private final EmailService emailService;
    private final UserServiceImpl userServiceImpl;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return  userServiceImpl.signin(loginRequest);
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userServiceImpl.signup(signupRequest));
    }

    @GetMapping("/activeAccount/{token}")
    public ResponseEntity<?> activeAccount(@PathVariable String token) {
        return ResponseEntity.ok(userServiceImpl.activeAccount(token));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // load user details from service
                    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(user.getUsername());
                    String token = jwtUtils.generateToken(userDetails);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is not authenticated!"));
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid user session!"));
        }

        // Xóa thông tin authentication khỏi SecurityContext
        SecurityContextHolder.clearContext();
        refreshTokenService.deleteByUserId(((UserDetailsImpl) principal).getId());
        return ResponseEntity.ok(new MessageResponse("User signed out successfully!"));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(userServiceImpl.forgotPassword(request.get("email")));
    }
    @PostMapping("/forgotPassword/{token}")
    public ResponseEntity<?> forgotPassword(@PathVariable String token, @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userServiceImpl.forgotPassword(token, request));
    }

}
