package com.lenin.hotel.authentication.utils;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.repository.ImageRepository;
import org.springframework.util.StringUtils;

public class UserUtils {

//    // Hàm buildUserResponse không thay đổi (dành cho các nơi gọi cũ)
//    public static UserResponse buildUserResponse(User user) {
//        return buildUserResponse(user, null); // Truyền null nếu không cần avatar
//    }

    // Hàm buildUserResponse với tham số imageRepository (dùng khi cần xử lý avatar)
    public static UserResponse buildUserResponse(User user, ImageRepository imageRepository) {
        // Lấy avatar nếu có ImageRepository
        String avatarUrl = null;
        if (imageRepository != null) {
            avatarUrl = imageRepository
                    .findByReferenceIdAndReferenceTableAndType(user.getId().intValue(), "users", ImageType.HOTEL)
                    .stream()
                    .findFirst()
                    .map(Image::getUrl)
                    .orElse(null);
        }

        // Xây dựng UserResponse
        return UserResponse.builder()
                .phone(user.getPhoneNumber())
                .email(user.getEmail())
                .username(user.getUsername())
                .id(user.getId())
                .address(user.getAddress())
                .roles(user.getRoles())
                .active(!StringUtils.hasText(user.getBanReason()))
                .avatarUrl(avatarUrl) // Set avatarUrl
                .build();
    }
}
