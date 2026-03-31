package com.example.backend.service.Auth;

import com.example.backend.dto.request.auth.LoginRequest;
import com.example.backend.dto.request.auth.RegisterRequest;
import com.example.backend.dto.response.auth.AuthResponse;
import com.example.backend.entities.Customer;
import com.example.backend.entities.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ICustomerRepository customerRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        if(customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new AppException("Số điện thoại đã tồn tại");
        }

        if(userRepository.existsByEmail(request.email())) {
            throw new AppException("Email đã tồn tại");
        }

        if(!request.password().equals(request.confirmPassword())) {
            throw new AppException("Xác nhận Mật khẩu không khớp");
        }

        Customer customer = Customer.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.CUSTOMER)
                .phoneNumber(request.phoneNumber())
                .loyaltyPoints(0)
                .build();
        Customer saveCustomer = customerRepository.save(customer);
        return new AuthResponse(
                saveCustomer.getId(),
                saveCustomer.getEmail(),
                saveCustomer.getFullName(),
                saveCustomer.getRole(),
                saveCustomer.isActive()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Email Không tồn tại"));

        if(!user.isActive()) {
            throw new AppException("Tài khoản đã bị khóa");
        }

        boolean matched = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if(!matched) {
            throw new AppException("Email hoặc mật khẩu không đúng");
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

        cookieService.addAccessTokenCookie(response,accessToken);
        cookieService.addRefreshTokenCookie(response,refreshToken);
        return new AuthResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole(),
                        user.isActive()
        );
    }

    @Override
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshToken(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResourceNotFoundException("Không tìm thấy refresh token");
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AppException("Refresh token không hợp lệ");
        }

        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (!user.isActive()) {
            throw new AppException("Tài khoản đã bị khóa");
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

        return "Làm mới token thành công";
    }

    @Override
    public String logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
        return "Đăng xuất thành công";
    }

    @Override
    public AuthResponse me(CustomUserPrincipal principal) {
        return new AuthResponse(
                principal.getUserId(),
                principal.getEmail(),
                principal.getFullName(),
                principal.getRole(),
                principal.isActive()
        );
    }


}
