package com.example.backend.controller.admin;

import com.example.backend.dto.request.admin.UpdateUserActiveRequest;
import com.example.backend.dto.response.admin.UserManagementResponse;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.service.admin.IAdminUserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final IAdminUserManagementService adminUserManagementService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserManagementResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(adminUserManagementService.getUsers(page, size), HttpStatus.OK);
    }

    @PatchMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserManagementResponse> updateUserActive(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserActiveRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return new ResponseEntity<>(
                adminUserManagementService.updateUserActive(userId, request, principal.getUserId()),
                HttpStatus.OK
        );
    }
}
