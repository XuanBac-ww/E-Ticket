package com.example.backend.controller.admin;

import com.example.backend.dto.request.admin.CreateStaffRequest;
import com.example.backend.dto.request.admin.UpdateStaffEventRequest;
import com.example.backend.dto.response.admin.StaffManagementResponse;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.service.admin.IAdminUserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staffs")
@RequiredArgsConstructor
public class AdminStaffManagementController {

    private final IAdminUserManagementService adminUserManagementService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<StaffManagementResponse>> getStaffs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(adminUserManagementService.getStaffs(page, size), HttpStatus.OK);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffManagementResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return new ResponseEntity<>(adminUserManagementService.createStaff(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{staffId}/managed-event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffManagementResponse> updateStaffManagedEvent(
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffEventRequest request
    ) {
        return new ResponseEntity<>(
                adminUserManagementService.updateStaffManagedEvent(staffId, request),
                HttpStatus.OK
        );
    }
}
