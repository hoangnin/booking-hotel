package com.lenin.hotel.authentication.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.authentication.request.BlockRequest;
import com.lenin.hotel.authentication.service.IAdminService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements IAdminService {
    private final UserRepository userRepository;

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
}
