package com.example.backend.service.Auth;

import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.RegisterRequest;
import com.example.backend.dto.response.auth.AuthResponse;
import com.example.backend.entities.Customer;
import com.example.backend.entities.User;
import com.example.backend.mapper.IAuthMapper;
import com.example.backend.repository.ICustomerRepository;
import com.example.backend.repository.IUserRepository;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.security.JwtService;
import com.example.backend.security.cookie.CookieService;
import com.example.backend.share.enums.UserRole;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ICustomerRepository customerRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final IAuthMapper authMapper;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering customer with email={}", maskEmail(request.email()));
        if (customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            log.warn("Registration rejected because phone number already exists");
            throw new AppException("Phone number already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected because email already exists email={}", maskEmail(request.email()));
            throw new AppException("Email already exists");
        }

        if (!request.password().equals(request.confirmPassword())) {
            log.warn("Registration rejected because password confirmation does not match email={}", maskEmail(request.email()));
            throw new AppException("Password confirmation does not match");
        }

        Customer customer = authMapper.createCustomerEntity(
                request,
                passwordEncoder.encode(request.password()),
                UserRole.CUSTOMER
        );
        Customer saveCustomer = customerRepository.save(customer);
        log.info("Registered customer userId={}, email={}", saveCustomer.getId(), maskEmail(saveCustomer.getEmail()));
        return authMapper.toResponse(saveCustomer);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        log.debug("Login attempt email={}", maskEmail(request.email()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Email does not exist"));

        if (!user.isActive()) {
            log.warn("Login rejected because account is inactive userId={}, email={}", user.getId(), maskEmail(user.getEmail()));
            throw new AppException("Account is locked");
        }

        boolean matched = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!matched) {
            log.warn("Login rejected because password mismatch userId={}, email={}", user.getId(), maskEmail(user.getEmail()));
            throw new AppException("Invalid email or password");
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive()
        );
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        cookieService.addAccessTokenCookie(response, accessToken);
        cookieService.addRefreshTokenCookie(response, refreshToken);
        log.info("Login succeeded userId={}, email={}", user.getId(), maskEmail(user.getEmail()));
        return authMapper.toResponse(user);
    }

    @Override
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshToken(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh token request rejected because refresh token is missing");
            throw new ResourceNotFoundException("Refresh token not found");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            log.warn("Refresh token request rejected because token is invalid");
            throw new AppException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            log.warn("Refresh token request rejected because account is inactive userId={}, email={}", user.getId(), maskEmail(user.getEmail()));
            throw new AppException("Account is locked");
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                user.getRole(),
                true
        );

        String newAccessToken = jwtService.generateAccessToken(principal);
        cookieService.addAccessTokenCookie(response, newAccessToken);
        log.info("Refresh token succeeded userId={}, email={}", user.getId(), maskEmail(user.getEmail()));

        return "Token refreshed successfully";
    }

    @Override
    public String logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
        log.info("Logout succeeded");
        return "Logged out successfully";
    }

    @Override
    public AuthResponse me(CustomUserPrincipal principal) {
        log.debug("Fetching current user profile userId={}", principal.getUserId());
        return authMapper.toResponse(principal);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() == 1) {
            return "*" + domainPart;
        }

        return localPart.charAt(0) + "***" + domainPart;
    }
}
