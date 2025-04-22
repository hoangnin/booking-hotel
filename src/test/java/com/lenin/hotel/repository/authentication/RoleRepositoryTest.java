package com.lenin.hotel.repository.authentication;

import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByName() {
        // Arrange: Check if the role already exists, and create it if not
        Optional<Role> existingRole = roleRepository.findByName(ERole.ROLE_USER);
        if (existingRole.isEmpty()) {
            Role role = new Role();
            role.setName(ERole.ROLE_USER);
            roleRepository.save(role);
        }

        // Act: Find the role by name
        Optional<Role> foundRole = roleRepository.findByName(ERole.ROLE_USER);

        // Assert: Verify the result
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo(ERole.ROLE_USER);
    }

   @Test
    public void testFindByName_NotFound() {
        // Arrange: Remove role references from users
        userRepository.findAll().forEach(user -> {
            user.getRoles().clear();
            userRepository.save(user);
        });

        // Delete all roles
        roleRepository.deleteAll();

        // Act: Attempt to find a role that does not exist
        Optional<Role> foundRole = roleRepository.findByName(ERole.ROLE_ADMIN);

        // Assert: Verify that the result is empty
        assertThat(foundRole).isNotPresent();
    }
}