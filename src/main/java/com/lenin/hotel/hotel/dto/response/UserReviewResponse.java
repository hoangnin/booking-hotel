package com.lenin.hotel.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReviewResponse {
    private String username;
    private String avatar;
}
