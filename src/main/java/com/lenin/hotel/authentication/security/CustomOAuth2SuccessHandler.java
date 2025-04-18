package com.lenin.hotel.authentication.security;

import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.repository.ImageRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String picture = oauthUser.getAttribute("picture");
        // check or create user
        User user = userRepository.getByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email); // hoặc sinh ngẫu nhiên
            newUser.setPassword(""); // vì không dùng password
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new ResourceNotFoundException("Role not found!")));
            newUser.setRoles(roles); // tùy bạn
            return userRepository.save(newUser);
        });
        if (user.getBanReason() != null && !user.getBanReason().isEmpty()){
            String redirectUrl = "http://localhost:5173/oauth2/failed";
            response.sendRedirect(redirectUrl);
            return;
        }

        // tạo token
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String jwt = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        List<Image> avatars = imageRepository.findByReferenceIdAndReferenceTableAndType(user.getId().intValue(), "users", ImageType.HOTEL);
        String avatarUrl = avatars.stream().findFirst().map(Image::getUrl).orElse(null);
        if (avatarUrl != null) {
            // Nếu avatar trong DB khác với ảnh Google
            if (picture != null && !picture.equals(avatarUrl)) {
                // Cập nhật ảnh mới nếu cần (tuỳ business rule)
                Image avatarImage = avatars.get(0);
                avatarImage.setUrl(picture);
                imageRepository.save(avatarImage);
                avatarUrl = picture;
            }
        } else {
            // Nếu chưa có ảnh, lưu ảnh Google
            if (picture != null) {
                Image newAvatar = new Image();
                newAvatar.setUrl(picture);
                newAvatar.setReferenceId(user.getId().intValue());
                newAvatar.setReferenceTable("users");
                newAvatar.setType(ImageType.HOTEL); // hoặc avatar, tuỳ enum của bạn
                imageRepository.save(newAvatar);
                avatarUrl = picture;
            }
        }

        // redirect hoặc trả JSON
        String redirectUrl = "http://localhost:5173/oauth2/success" +
                "?token=" + jwt +
                "&refresh=" + refreshToken.getToken() +
                "&avatar=" + URLEncoder.encode(avatarUrl == null ? "" : avatarUrl, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}

