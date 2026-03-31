package com.example.backend.service.user;

import com.example.backend.dto.request.auth.ChangePasswordRequest;
import com.example.backend.dto.request.user.UpdateProfileRequest;
import com.example.backend.security.CustomUserPrincipal;

import java.util.Map;

public interface IUserService {

    Map<String,Object> getMyProfile(CustomUserPrincipal principal);
    String updateProfile(CustomUserPrincipal principal, UpdateProfileRequest request);
    String changePassword(CustomUserPrincipal principal, ChangePasswordRequest request);
}
