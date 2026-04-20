package com.example.backend.service.admin;

import com.example.backend.dto.request.admin.CreateStaffRequest;
import com.example.backend.dto.request.admin.UpdateStaffEventRequest;
import com.example.backend.dto.request.admin.UpdateUserActiveRequest;
import com.example.backend.dto.response.admin.StaffManagementResponse;
import com.example.backend.dto.response.admin.UserManagementResponse;
import com.example.backend.dto.response.api.PageResponse;

public interface IAdminUserManagementService {

    PageResponse<UserManagementResponse> getUsers(int page, int size);

    UserManagementResponse updateUserActive(Long userId, UpdateUserActiveRequest request, Long actingAdminId);

    PageResponse<StaffManagementResponse> getStaffs(int page, int size);

    StaffManagementResponse createStaff(CreateStaffRequest request);

    StaffManagementResponse updateStaffManagedEvent(Long staffId, UpdateStaffEventRequest request);
}
