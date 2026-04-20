package com.example.backend.mapper;

import com.example.backend.dto.request.admin.CreateStaffRequest;
import com.example.backend.dto.response.admin.StaffManagementResponse;
import com.example.backend.dto.response.admin.UserManagementResponse;
import com.example.backend.entities.Staff;
import com.example.backend.entities.User;
import com.example.backend.share.enums.UserRole;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IAdminUserManagementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "active", constant = "true")
    Staff createStaffEntity(CreateStaffRequest request, String passwordHash, UserRole role);

    UserManagementResponse toUserResponse(User user);

    StaffManagementResponse toStaffResponse(Staff staff);
}
