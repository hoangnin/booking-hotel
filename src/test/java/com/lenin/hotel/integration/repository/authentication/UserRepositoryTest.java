package com.lenin.hotel.integration.repository.authentication;

import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.TestDynamicProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
@Import(DatabaseTestContainer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTest extends TestDynamicProperties {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        try {
            // Clear tables in correct order - respecting foreign key constraints
            jdbcTemplate.execute("DELETE FROM price_tracking"); // Add this line first
            jdbcTemplate.execute("DELETE FROM hotel_amenity");
            jdbcTemplate.execute("DELETE FROM refresh_token");
            jdbcTemplate.execute("DELETE FROM hotels");
            jdbcTemplate.execute("DELETE FROM user_role");
            jdbcTemplate.execute("DELETE FROM users");
            jdbcTemplate.execute("DELETE FROM roles");
        } catch (Exception e) {
            System.err.println("Database cleanup error: " + e.getMessage());
        }
    }

    @Test
    public void testFindByUsername() {
        // Create with unique values
        String uniqueUsername = "testuser-" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = "email-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.getByUsername(uniqueUsername);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(uniqueUsername);
    }

    @Test
    public void testFindByEmail() {
        // Generate unique identifiers
        String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@example.com";

        // Arrange
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.getByEmail(uniqueEmail);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    public void testFindAllByRoles() {
        // Generate unique identifiers
        String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@example.com";

        // Arrange
        Role role = new Role();
        role.setName(ERole.ROLE_USER);
        roleRepository.save(role);

        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        user.addRole(role);
        userRepository.save(user);

        // Act
        Optional<List<User>> usersWithRole = userRepository.findAllByRoles(Set.of(role));

        // Assert
        assertThat(usersWithRole).isPresent();
        assertThat(usersWithRole.get()).hasSize(1);
        assertThat(usersWithRole.get().get(0).getUsername()).isEqualTo(uniqueUsername);
    }

    @Test
    public void testExistsByUsername() {
        // Generate unique identifiers
        String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@example.com";

        // Arrange
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername(uniqueUsername);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmail() {
        // Generate unique identifiers
        String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@example.com";

        // Arrange
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail(uniqueEmail);

        // Assert
        assertThat(exists).isTrue();
    }

    // Existing "not found" tests remain unchanged
}