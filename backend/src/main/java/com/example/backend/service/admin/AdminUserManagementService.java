package com.example.backend.service.admin;

import com.example.backend.dto.request.admin.CreateStaffRequest;
import com.example.backend.dto.request.admin.UpdateStaffEventRequest;
import com.example.backend.dto.request.admin.UpdateUserActiveRequest;
import com.example.backend.dto.response.admin.StaffManagementResponse;
import com.example.backend.dto.response.admin.UserManagementResponse;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.entities.Staff;
import com.example.backend.entities.User;
import com.example.backend.mapper.IAdminUserManagementMapper;
import com.example.backend.repository.IEventRepository;
import com.example.backend.repository.IStaffRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.share.enums.UserRole;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService implements IAdminUserManagementService {

    private final IUserRepository userRepository;
    private final IStaffRepository staffRepository;
    private final IEventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAdminUserManagementMapper adminUserManagementMapper;

    @Override
    public PageResponse<UserManagementResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> users = userRepository.findAll(pageable);
        List<UserManagementResponse> data = users.getContent().stream()
                .map(adminUserManagementMapper::toUserResponse)
                .toList();

        return new PageResponse<>(
                200,
                true,
                "Users retrieved successfully",
                data,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
    }

    @Override
    @Transactional
    public UserManagementResponse updateUserActive(Long userId, UpdateUserActiveRequest request, Long actingAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Objects.equals(userId, actingAdminId) && Boolean.FALSE.equals(request.active())) {
            throw new AppException("You cannot deactivate your own account");
        }

        user.setActive(request.active());
        return adminUserManagementMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public PageResponse<StaffManagementResponse> getStaffs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Staff> staffs = staffRepository.findAll(pageable);
        List<StaffManagementResponse> data = staffs.getContent().stream()
                .map(adminUserManagementMapper::toStaffResponse)
                .toList();

        return new PageResponse<>(
                200,
                true,
                "Staffs retrieved successfully",
                data,
                staffs.getNumber(),
                staffs.getSize(),
                staffs.getTotalElements(),
                staffs.getTotalPages(),
                staffs.isLast()
        );
    }

    @Override
    @Transactional
    public StaffManagementResponse createStaff(CreateStaffRequest request) {
        validateStaffCreateRequest(request);
        validateManagedEventExists(request.managedEventId());

        Staff staff = adminUserManagementMapper.createStaffEntity(
                request,
                passwordEncoder.encode(request.password()),
                UserRole.STAFF
        );

        return adminUserManagementMapper.toStaffResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public StaffManagementResponse updateStaffManagedEvent(Long staffId, UpdateStaffEventRequest request) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        validateManagedEventExists(request.managedEventId());
        staff.setManagedEventId(request.managedEventId());

        return adminUserManagementMapper.toStaffResponse(staffRepository.save(staff));
    }

    private void validateStaffCreateRequest(CreateStaffRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException("Email already exists");
        }

        if (staffRepository.existsByStaffCode(request.staffCode())) {
            throw new AppException("Staff code already exists");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new AppException("Password confirmation does not match");
        }
    }

    private void validateManagedEventExists(Long managedEventId) {
        if (managedEventId == null) {
            return;
        }

        if (!eventRepository.existsById(managedEventId)) {
            throw new ResourceNotFoundException("Event not found");
        }
    }

}
