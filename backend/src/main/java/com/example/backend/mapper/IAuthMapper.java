package com.example.backend.mapper;

import com.example.backend.dto.request.auth.RegisterRequest;
import com.example.backend.dto.response.auth.AuthResponse;
import com.example.backend.entities.Customer;
import com.example.backend.entities.User;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.share.enums.UserRole;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IAuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "loyaltyPoints", constant = "0")
    Customer createCustomerEntity(RegisterRequest request, String passwordHash, UserRole role);

    @Mapping(target = "userId", source = "id")
    AuthResponse toResponse(User user);

    @Mapping(target = "userId", source = "userId")
    AuthResponse toResponse(CustomUserPrincipal principal);
}
