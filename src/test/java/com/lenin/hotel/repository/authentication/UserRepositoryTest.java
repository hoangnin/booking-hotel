package com.lenin.hotel.repository.authentication;

import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Act
        User foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindByEmail() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.getByEmail("testuser@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    public void testFindAllByRoles() {
        // Arrange
        Role role = new Role();
        role.setName(ERole.ROLE_USER);
        roleRepository.save(role);

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.addRole(role);
        userRepository.save(user);

        // Act
        Optional<List<User>> usersWithRole = userRepository.findAllByRoles(Set.of(role));

        // Assert
        assertThat(usersWithRole).isPresent();
        assertThat(usersWithRole.get()).hasSize(1);
        assertThat(usersWithRole.get().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testExistsByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmail() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("testuser@example.com");

        // Assert
        assertThat(exists).isTrue();
    }
    @Test
    public void testFindByUsername_NotFound() {
        // Act
        User foundUser = userRepository.findByUsername("nonexistentuser");

        // Assert
        assertThat(foundUser).isNull();
    }

    @Test
    public void testFindByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.getByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    public void testFindAllByRoles_NotFound() {
        // Arrange
        Role role = new Role();
        role.setName(ERole.ROLE_ADMIN); // Assuming no users have this role
        roleRepository.save(role);

        // Act
        Optional<List<User>> usersWithRole = userRepository.findAllByRoles(Set.of(role));

        // Assert
        assertThat(usersWithRole).isPresent();
        assertThat(usersWithRole.get()).isEmpty();
    }

    @Test
    public void testExistsByUsername_NotFound() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    public void testExistsByEmail_NotFound() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }
}