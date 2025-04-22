package com.lenin.hotel.controller.authentication;

import com.lenin.hotel.authentication.controller.AdminController;
import com.lenin.hotel.authentication.dto.request.BlockRequest;
import com.lenin.hotel.authentication.service.impl.AdminServiceImpl;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.common.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private AdminServiceImpl adminServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBlockAccess() {
        // Arrange
        BlockRequest blockRequest = new BlockRequest();
        blockRequest.setUserId(1L);
        blockRequest.setBanReason("Test ban reason");

        when(adminServiceImpl.blockUser(blockRequest)).thenReturn(Map.of("message", "Ban user Success"));

        // Act
        ResponseEntity<?> responseEntity = adminController.blockAccess(blockRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Map.of("message", "Ban user Success"), responseEntity.getBody());
    }

    @Test
    void testUnBlockAccess() {
        // Arrange
        long userId = 1L;
        when(adminServiceImpl.unBlockUser(userId)).thenReturn(Map.of("message", "Un ban user Success"));

        // Act
        ResponseEntity<?> responseEntity = adminController.unBlockAccess(userId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Map.of("message", "Un ban user Success"), responseEntity.getBody());
    }

    @Test
    void testGetUsers() {
        // Arrange
        int page = 0;
        int size = 10;
        List<UserResponse> content = List.of();
        long totalElements = 0;
        int totalPages = 0;
        boolean last = true;
        PagedResponse<UserResponse> mockUsers = new PagedResponse<>(content, page, size, totalElements, totalPages, last);
        when(adminServiceImpl.getUsers(page, size)).thenReturn(mockUsers);

        // Act
        ResponseEntity<?> responseEntity = adminController.getUsers(page, size);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockUsers, responseEntity.getBody());
    }

    @Test
    void testSearchUsers() {
        // Arrange
        String keySearch = "test";
        List<UserResponse> mockSearchResults = List.of(); // Replace with actual mock data if needed
        when(adminServiceImpl.searchUser(keySearch)).thenReturn(mockSearchResults);

        // Act
        ResponseEntity<?> responseEntity = adminController.searchUsers(keySearch);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockSearchResults, responseEntity.getBody());
    }
}