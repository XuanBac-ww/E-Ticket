package com.example.backend.dto.response.auth;

import com.example.backend.share.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AuthResponse {
    Long userId;
    String email;
    String fullName;
    UserRole role;
    boolean active;
}
