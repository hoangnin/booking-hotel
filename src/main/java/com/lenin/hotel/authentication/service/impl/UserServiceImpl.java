package com.lenin.hotel.authentication.service.impl;

import com.lenin.hotel.authentication.dto.request.*;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.dto.response.JwtResponse;
import com.lenin.hotel.authentication.security.JwtUtil;
import com.lenin.hotel.authentication.security.UserDetailsImpl;
import com.lenin.hotel.authentication.service.IUserService;
import com.lenin.hotel.authentication.security.RefreshTokenService;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.service.IEmailService;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.dto.request.ImageRequest;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lenin.hotel.authentication.utils.UserUtils.buildUserResponse;
import static com.lenin.hotel.common.utils.SecurityUtils.getCurrentUsername;
import static com.lenin.hotel.hotel.utils.ImageUtils.buildImage;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService IEmailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final ImageRepository imageRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z");

    public Map<String, String> signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ResourceNotFoundException("Username is already taken!");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BusinessException("Email is already in use!");
        }

        // create new user
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new ResourceNotFoundException("Role not found!")));
        user.setRoles(roles);
        user.setBanReason("Not active yet");
        userRepository.save(user);
        IEmailService.sendMailActiveAccount(user.getUsername(), user.getEmail(), jwtUtil.generateToken(user.getEmail()));
        return Map.of("message","User registered successfully! Please check your email to activate your account!");
    }

    public ResponseEntity<?> signin(LoginRequest loginRequest) {
        User user = userRepository.getByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBanUntil() != null && user.getBanUntil().isAfter(ZonedDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Your account has been banned until " + formatter.format(user.getBanUntil())));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            // reset login fail count
            user.setFailedLoginAttempts(0);
            user.setBanUntil(null);
            userRepository.save(user);

            String jwt = jwtUtil.generateToken(userDetails);
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            //avatar
            List<Image> avatars = imageRepository.findByReferenceIdAndReferenceTableAndType(userDetails.getId().intValue(), "users", ImageType.HOTEL);
            String avatarUrl = avatars.stream().findFirst().map(Image::getUrl).orElse(null);

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, avatarUrl));
        } catch (BadCredentialsException e) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setBanUntil(ZonedDateTime.now().plusMinutes(2));
            }
            userRepository.save(user);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid username or password"));
        }
    }

    private Map<String, String> changePasswordProcess(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        String username = getCurrentUsername();
        User user = userRepository.getByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return Map.of("message", "Password changed successfully");
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        return changePasswordProcess(request);
    }

    @Override
    public void updateProfile(UpdateProfileRequest update) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));
        user.setEmail(update.getEmail());
        user.setAddress(update.getAddress());
        user.setPhoneNumber(update.getPhoneNumber());
        if (update.getAvatarUrl() != null) {
            List<Image> image = imageRepository.findByReferenceIdAndReferenceTableAndType(user.getId().intValue(), "users", ImageType.HOTEL);
            if (!image.isEmpty()) {
                image.get(0).setUrl(update.getAvatarUrl());
                imageRepository.save(image.get(0));
            } else {
                ImageRequest imageRequest = ImageRequest.builder()
                        .url(update.getAvatarUrl())
                        .type(ImageType.HOTEL)
                        .build();
                Image newImage = buildImage(imageRequest, user.getId().intValue(), "users");
                imageRepository.save(newImage);
            }
        }
        userRepository.save(user);
    }

    public Map<String, String> forgotPassword(String email) {
        if (userRepository.existsByEmail(email)) {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return Map.of("message", "If your email exist in our system, then you will receive instructions on resetting your password.");
            }
            if (user.getBanUntil() != null && user.getBanUntil().isAfter(ZonedDateTime.now())) {
                throw new BusinessException("Your account has been banned until " + formatter.format(user.getBanUntil()));
            }
            IEmailService.sendMailForgotPassword(user.getUsername(), user.getEmail(), jwtUtil.generateToken(email));
        }
        return Map.of("message", "If your email exist in our system, then you will receive instructions on resetting your password.");
    }

    public Map<String, String> forgotPassword(String token, ForgotPasswordRequest request) {
        boolean validToken = jwtUtil.validateJwtToken(token);
        if (validToken) {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new BusinessException("Passwords do not match");
            }
            String email = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.getByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return Map.of("message", "Password changed successfully");
        } else
            throw new BusinessException("Invalid or expired token, please try again ");
    }

    public Map<String, String> activeAccount(String token) {
        boolean validToken = jwtUtil.validateJwtToken(token);
        if (validToken) {
            String email = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.getByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
            user.setBanReason(null);
            userRepository.save(user);
            IEmailService.sendMailSignupSuccess(user.getUsername(), user.getEmail());
            return Map.of("message", "Account activated successfully");
        } else
            throw new BusinessException("Invalid or expired token, please try again ");
    }

    @Override
    public UserResponse getHotelOwner(Integer id) {
        User user = userRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel owner id not found!"));

        // Chỉ trả về nếu user có role HOTEL_OWNER
        boolean isHotelOwner = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_HOTEL));

        if (!isHotelOwner) {
            throw new ResourceNotFoundException("User is not a hotel owner!");
        }

        UserResponse userResponse = buildUserResponse(user, imageRepository);



        return userResponse;
    }

    @Override
    public UserResponse getUserInfo() {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        UserResponse userResponse = buildUserResponse(user, imageRepository);
        return userResponse;
    }


}

