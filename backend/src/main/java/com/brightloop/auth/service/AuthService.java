package com.brightloop.auth.service;

import com.brightloop.auth.dto.AuthResponse;
import com.brightloop.auth.dto.ForgotPasswordRequest;
import com.brightloop.auth.dto.LoginRequest;
import com.brightloop.auth.dto.PasswordResetRequest;
import com.brightloop.auth.dto.RefreshTokenRequest;
import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.dto.UserDto;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletResponse response);

    void logout(UUID userId, HttpServletResponse response);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(PasswordResetRequest request);

    UserDto getCurrentUser(UUID userId);
}
