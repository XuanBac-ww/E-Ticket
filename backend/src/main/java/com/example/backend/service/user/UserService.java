package com.example.backend.service.user;

import com.example.backend.dto.request.auth.ChangePasswordRequest;
import com.example.backend.dto.request.user.UpdateProfileRequest;
import com.example.backend.entities.Admin;
import com.example.backend.entities.Customer;
import com.example.backend.entities.Staff;
import com.example.backend.entities.User;
import com.example.backend.repository.IAdminRepository;
import com.example.backend.repository.ICustomerRepository;
import com.example.backend.repository.IStaffRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.share.enums.UserRole;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final ICustomerRepository customerRepository;
    private final IStaffRepository staffRepository;
    private final IAdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getMyProfile(CustomUserPrincipal principal) {
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email does not exist"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("fullName", user.getFullName());
        data.put("active", user.isActive());

        if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            data.put("phoneNumber", customer.getPhoneNumber());
            data.put("loyaltyPoints", customer.getLoyaltyPoints());
        }

        if (user.getRole() == UserRole.STAFF) {
            Staff staff = staffRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
            data.put("staffCode", staff.getStaffCode());
            data.put("managedEventId", staff.getManagedEventId());
        }

        if (user.getRole() == UserRole.ADMIN) {
            Admin admin = adminRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
            data.put("adminCode", admin.getAdminCode());
        }

        return data;
    }

    @Override
    @Transactional
    public String updateProfile(CustomUserPrincipal principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            customer.setPhoneNumber(request.phoneNumber());
            customer.setFullName(request.fullName());
            customerRepository.save(customer);
        }

        userRepository.save(user);
        return "Profile updated successfully";
    }

    @Override
    @Transactional
    public String changePassword(CustomUserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new AppException("Current password is incorrect");
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new AppException("New password confirmation does not match");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return "Password changed successfully";
    }
}
