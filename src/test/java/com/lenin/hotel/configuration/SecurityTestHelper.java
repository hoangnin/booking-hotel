package com.lenin.hotel.configuration;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.repository.RoleRepository;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SecurityTestHelper {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TestRestTemplate restTemplate;

    public SecurityTestHelper(UserRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder,
                              TestRestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }
    public String getTestUsername(){
        return "admin";
    }

    public String getAuthHeader() {
        // Create roles if they do not exist.
        ERole[] roleEnums = {ERole.ROLE_USER, ERole.ROLE_HOTEL, ERole.ROLE_ADMIN};
        for (ERole roleEnum : roleEnums) {
            Optional<Role> roleOpt = roleRepository.findByName(roleEnum);
            if (roleOpt.isEmpty()) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
            }
        }

        String username = "admin";
        String rawPassword = "SecurePass123@";
        // Create the user if it does not exist.
        if (userRepository.findByUsername(username) == null) {
            User user = new User();
            user.setUsername(username);
            user.setEmail("lenin5262622313165.hn@gmail.com");
            user.setPassword(passwordEncoder.encode(rawPassword));
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName(ERole.ROLE_USER).get());
            roles.add(roleRepository.findByName(ERole.ROLE_HOTEL).get());
            roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            user.setRoles(roles);
            userRepository.save(user);
        }

        // Login using the /api/auth/signin endpoint to get the token.
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", rawPassword);
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/api/auth/signin", loginRequest, Map.class);
        if (loginResponse.getStatusCode() == HttpStatus.OK) {
            Map<?, ?> body = loginResponse.getBody();
            String token = (String) body.get("token");
            return "Bearer " + token;
        }
        throw new RuntimeException("Login failed in SecurityTestHelper");
    }
}