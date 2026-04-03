package com.example.backend.controller;

import com.example.backend.dto.request.auth.ChangePasswordRequest;
import com.example.backend.dto.request.user.UpdateProfileRequest;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<Map<String,Object>> getMyProfile(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return new ResponseEntity<>(userService.getMyProfile(principal), HttpStatus.OK);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> updateMyProfile(@AuthenticationPrincipal CustomUserPrincipal principal,
                                            @Valid @RequestBody UpdateProfileRequest request) {
        return new ResponseEntity<>(userService.updateProfile(principal,request),HttpStatus.OK);
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                  @Valid @RequestBody ChangePasswordRequest request) {
        return new ResponseEntity<>(userService.changePassword(principal,request),HttpStatus.OK);
    }
}
