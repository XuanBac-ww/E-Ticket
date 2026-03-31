package com.example.backend.dto.response.jwt;

import com.example.backend.share.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtClaims {
    private Long userId;
    private String email;
    private UserRole role;
    private String type;
}
