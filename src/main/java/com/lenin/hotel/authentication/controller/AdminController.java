// Java
package com.lenin.hotel.authentication.controller;

import com.lenin.hotel.authentication.dto.request.BlockRequest;
import com.lenin.hotel.authentication.service.impl.AdminServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {
    private final AdminServiceImpl adminServiceImpl;

    @PostMapping(value = "/block", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> blockAccess(@RequestBody BlockRequest blockRequest) {
        return ResponseEntity.ok().body(adminServiceImpl.blockUser(blockRequest));
    }

    @GetMapping(value = "/unBlock/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> unBlockAccess(@PathVariable Long userId) {
        return ResponseEntity.ok().body(adminServiceImpl.unBlockUser(userId));
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok().body(adminServiceImpl.getUsers(page, size));
    }

    @GetMapping(value = "/users/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchUsers(@RequestParam String keySearch) {
        return ResponseEntity.ok().body(adminServiceImpl.searchUser(keySearch));
    }
}