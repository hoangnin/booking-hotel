package com.lenin.hotel.service.authentication;

import com.lenin.hotel.authentication.dto.request.BlockRequest;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.service.impl.AdminServiceImpl;
import com.lenin.hotel.common.PagedResponse;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user;
    private BlockRequest blockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Role userRole = new Role();
        userRole.setId(2);
        userRole.setName(ERole.ROLE_USER);
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(userRole));

        blockRequest = new BlockRequest();
        blockRequest.setUserId(1L);
        blockRequest.setBanReason("Violation of terms");
    }

    @Test
    void testBlockUser_Success() {
        // Arrange
        when(userRepository.getById(blockRequest.getUserId())).thenReturn(Optional.of(user));

        // Act
        Map<String, String> response = adminService.blockUser(blockRequest);

        // Assert
        assertEquals("Ban user Success", response.get("message"));
        assertEquals("Violation of terms", user.getBanReason());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testBlockUser_UserNotFound() {
        // Arrange
        when(userRepository.getById(blockRequest.getUserId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.blockUser(blockRequest));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUnBlockUser_Success() {
        // Arrange
        when(userRepository.getById(blockRequest.getUserId())).thenReturn(Optional.of(user));

        // Act
        Map<String, String> response = adminService.unBlockUser(user.getId());

        // Assert
        assertEquals("Un ban user Success", response.get("message"));
        assertNull(user.getBanReason());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUnBlockUser_UserNotFound() {
        // Arrange
        when(userRepository.getById(blockRequest.getUserId())).thenReturn(Optional.empty());
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.unBlockUser(user.getId()));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUsers_Success() {
        // Arrange
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName(ERole.ROLE_ADMIN);
        User adminUser = new User();
        adminUser.setRoles(Set.of(adminRole));

        Page<User> usersPage = new PageImpl<>(List.of(user, adminUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(usersPage);
        when(imageRepository.findByReferenceIdAndReferenceTableAndType(anyInt(), anyString(), any()))
                .thenReturn(List.of(new Image()));

        // Act
        PagedResponse<UserResponse> response = adminService.getUsers(0, 10);

        // Assert
        assertEquals(1, response.content().size()); // Only non-admin users
        assertEquals("testUser", response.content().get(0).getUsername());
        verify(userRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void testSearchUser_Success() {
        // Arrange
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("test", "test"))
                .thenReturn(List.of(user));
        when(imageRepository.findByReferenceIdAndReferenceTableAndType(anyInt(), anyString(), any()))
                .thenReturn(List.of(new Image()));

        // Act
        List<UserResponse> responses = adminService.searchUser("test");

        // Assert
        assertEquals(1, responses.size());
        assertEquals("testUser", responses.get(0).getUsername());
        verify(userRepository, times(1))
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("test", "test");
    }

    @Test
    void testSearchUser_NoResults() {
        // Arrange
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("unknown", "unknown"))
                .thenReturn(List.of());

        // Act
        List<UserResponse> responses = adminService.searchUser("unknown");

        // Assert
        assertTrue(responses.isEmpty());
        verify(userRepository, times(1))
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("unknown", "unknown");
    }
}