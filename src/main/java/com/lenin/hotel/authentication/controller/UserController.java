package com.lenin.hotel.authentication.controller;

import com.lenin.hotel.authentication.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.ok(userServiceImpl.resetPassword(resetPasswordRequest));
    }


}
