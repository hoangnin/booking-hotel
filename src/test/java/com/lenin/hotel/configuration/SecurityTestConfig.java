package com.lenin.hotel.configuration;

import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.repository.RoleRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.web.client.TestRestTemplate;

@TestConfiguration
public class SecurityTestConfig {

    @Bean
    public SecurityTestHelper securityTestHelper(UserRepository userRepository,
                                                 RoleRepository roleRepository,
                                                 PasswordEncoder passwordEncoder,
                                                 TestRestTemplate restTemplate) {
        return new SecurityTestHelper(userRepository, roleRepository, passwordEncoder, restTemplate);
    }
}