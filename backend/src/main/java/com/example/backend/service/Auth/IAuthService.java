package com.example.backend.service.Auth;

import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.RegisterRequest;
import com.example.backend.dto.response.auth.AuthResponse;
import com.example.backend.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletResponse response);

    String refresh(HttpServletRequest request,HttpServletResponse response);
    String logout(HttpServletResponse response);
    AuthResponse me(CustomUserPrincipal principal);
}
