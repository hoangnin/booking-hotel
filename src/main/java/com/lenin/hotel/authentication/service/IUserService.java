package com.lenin.hotel.authentication.service;

import com.lenin.hotel.authentication.dto.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.dto.request.UpdateProfileRequest;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import jakarta.validation.Valid;

public interface IUserService {

    UserResponse getHotelOwner(Integer id);

    Object getUserInfo();

    Object resetPassword(@Valid ResetPasswordRequest resetPasswordRequest);

    void updateProfile(@Valid UpdateProfileRequest update);
}
