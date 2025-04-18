package com.lenin.hotel.authentication.controller;

import com.lenin.hotel.authentication.dto.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.dto.request.UpdateProfileRequest;
import com.lenin.hotel.authentication.service.IUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final IUserService userService;

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequest));
    }
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }
    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest update){
        userService.updateProfile(update);
        return ResponseEntity.ok(Map.of("message", "Update success"));
    }


}
