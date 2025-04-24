package com.lenin.hotel.unit.service.authentication;

import com.lenin.hotel.authentication.dto.request.*;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.security.JwtUtil;
import com.lenin.hotel.authentication.security.RefreshTokenService;
import com.lenin.hotel.authentication.security.UserDetailsImpl;
import com.lenin.hotel.authentication.service.impl.UserServiceImpl;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.common.service.IEmailService;
import com.lenin.hotel.hotel.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;


    @Mock
    private IEmailService emailService;

    @Mock
    private ImageRepository imageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(ERole.ROLE_USER);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(userRole));

        // Mock SecurityContext and Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testUser");

        SecurityContextHolder.setContext(securityContext);

        // Mock RefreshTokenService
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mockRefreshToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        // Mock ImageRepository
        when(imageRepository.findByReferenceIdAndReferenceTableAndType(anyInt(), anyString(), any(ImageType.class)))
                .thenReturn(List.of()); // Return an empty list or mock images as needed
    }

    @Test
    void testSignup_Success() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newUser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        Role role = new Role();
        role.setId(1);
        role.setName(ERole.ROLE_USER);
        role.setCreateDt(null);
        role.setUpdateDt(null);

        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        // Act
        var response = userService.signup(signupRequest);

        // Assert
        assertEquals("User registered successfully! Please check your email to activate your account!", response.get("message"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_UsernameExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testUser");
        signupRequest.setEmail("new@example.com");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.signup(signupRequest));
    }

    @Test
    void testSignin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        Set<Role> roles = user.getRoles();
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name())) // Convert roles to GrantedAuthority
                .collect(Collectors.toList());

        UserDetailsImpl userDetails = new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                null, // No ban reason
                authorities
        );

        Authentication authentication = mock(Authentication.class);

        when(userRepository.getByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails); // Return the mocked UserDetailsImpl
        when(jwtUtil.generateToken(anyString())).thenReturn("jwtToken");

        // Act
        var response = userService.signin(loginRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testSignin_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.getByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        // Act
        var response = userService.signin(loginRequest);

        // Assert
        // Corrected code
        assertEquals("Invalid username or password", ((Map<String, String>) response.getBody()).get("message"));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        // Act
        var response = userService.resetPassword(request);

        // Assert
        assertEquals("Password changed successfully", response.get("message"));
        verify(userRepository, times(1)).save(user);
    }

   @Test
    void testForgotPassword_Success() {
        // Arrange
        String email = "test@example.com";
        user.setBanUntil(null);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(userRepository.getByEmail(email)).thenReturn(Optional.of(user)); // Ensure user is returned
        when(jwtUtil.generateToken(email)).thenReturn("resetToken");

        // Act
        var response = userService.forgotPassword(email);

        // Assert
        assertEquals("If your email exist in our system, then you will receive instructions on resetting your password.", response.get("message"));
    }

    @Test
    void testGetUserInfo_Success() {
        // Arrange
        when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(user));

        // Act
        var response = userService.getUserInfo();

        // Assert
        assertNotNull(response);
        assertEquals("testUser", response.getUsername());
    }
}