package com.brightloop.auth.controller;

import com.brightloop.auth.dto.AuthResponse;
import com.brightloop.auth.dto.ForgotPasswordRequest;
import com.brightloop.auth.dto.LoginRequest;
import com.brightloop.auth.dto.PasswordResetRequest;
import com.brightloop.auth.dto.RefreshTokenRequest;
import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.dto.UserDto;
import com.brightloop.auth.service.AuthService;
import com.brightloop.common.dto.ApiResponse;
import com.brightloop.security.jwt.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response
    ) {
        if (principal != null) {
            authService.logout(principal.getId(), response);
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("If an account exists, a password reset link has been dispatched.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been successfully reset. Please log in.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDto userDto = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }
}
