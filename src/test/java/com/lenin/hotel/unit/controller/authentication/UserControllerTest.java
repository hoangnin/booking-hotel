package com.lenin.hotel.unit.controller.authentication;

import com.lenin.hotel.authentication.controller.UserController;
import com.lenin.hotel.authentication.dto.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.dto.request.UpdateProfileRequest;
import com.lenin.hotel.authentication.service.IUserService;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private IUserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testResetPassword_Success() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        Map<String, String> expectedResponse = Map.of("message", "Password changed successfully");
        when(userService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = userController.resetPassword(request);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(userService, times(1)).resetPassword(request);
    }

    @Test
    void testGetUserInfo_Success() {
        // Arrange
        UserResponse userResponse = UserResponse.builder().build();
        userResponse.setUsername("testUser");
        when(userService.getUserInfo()).thenReturn(userResponse);

        // Act
        ResponseEntity<?> response = userController.getUserInfo();

        // Assert
        assertEquals(userResponse, response.getBody());
        verify(userService, times(1)).getUserInfo();
    }

    @Test
    void testUpdateProfile_Success() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("newemail@example.com");
        request.setAddress("New Address");
        request.setPhoneNumber("123456789");

        Map<String, String> expectedResponse = Map.of("message", "Update success");
        doNothing().when(userService).updateProfile(any(UpdateProfileRequest.class));

        // Act
        ResponseEntity<?> response = userController.updateProfile(request);

        // Assert
        assertEquals(expectedResponse, response.getBody());
        verify(userService, times(1)).updateProfile(request);
    }
}