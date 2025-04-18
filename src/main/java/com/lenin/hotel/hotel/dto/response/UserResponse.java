package com.lenin.hotel.hotel.dto.response;

import com.lenin.hotel.authentication.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class UserResponse {
    private String avatarUrl;
    private String username;
    private String email;
    private String phone;
    private Set<Role> roles;
    private Long id;
    private String address;
    private boolean active;
}
