package com.example.backend.controller;

import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.RegisterRequest;
import com.example.backend.dto.response.auth.AuthResponse;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.service.Auth.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                  HttpServletResponse response) {
        return new ResponseEntity<>(authService.login(request,response), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        return new ResponseEntity<>(authService.refresh(request,response),HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        return new ResponseEntity<>(authService.logout(response),HttpStatus.OK);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return new ResponseEntity<>(
                Map.of(
                        "token", csrfToken.getToken(),
                        "headerName", csrfToken.getHeaderName(),
                        "parameterName", csrfToken.getParameterName()
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return new ResponseEntity<>(authService.me(principal), HttpStatus.OK);
    }

}
