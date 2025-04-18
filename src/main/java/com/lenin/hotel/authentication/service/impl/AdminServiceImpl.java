package com.lenin.hotel.authentication.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.dto.request.BlockRequest;
import com.lenin.hotel.authentication.service.IAdminService;
import com.lenin.hotel.authentication.utils.UserUtils;
import com.lenin.hotel.common.PagedResponse;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.repository.ImageRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lenin.hotel.authentication.utils.UserUtils.buildUserResponse;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements IAdminService {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public Map<String, String> blockUser(BlockRequest blockRequest) {
        User user = userRepository.getById(blockRequest.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanReason(blockRequest.getBanReason());
        userRepository.save(user);
        return Map.of("message", "Ban user Success");
    }
    public Map<String, String> unBlockUser(long userId){
        User user = userRepository.getById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanReason(null);
        userRepository.save(user);
        return Map.of("message", "Un ban user Success");
    }

    @Override
    public PagedResponse<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAll(pageable);

        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : usersPage.getContent()) {
            if (user.getRoles().stream()
                    .noneMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
                UserResponse userResponse = buildUserResponse(user, imageRepository);
                userResponses.add(userResponse);
            }
        }

        return new PagedResponse<>(
                userResponses,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }

    @Override
    public List<UserResponse> searchUser(String keyword) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        return users.stream().map(user -> buildUserResponse(user, imageRepository)).collect(Collectors.toList());
    }


}
