package com.brightloop.auth.service.impl;

import com.brightloop.auth.dto.AuthResponse;
import com.brightloop.auth.dto.ForgotPasswordRequest;
import com.brightloop.auth.dto.LoginRequest;
import com.brightloop.auth.dto.PasswordResetRequest;
import com.brightloop.auth.dto.RefreshTokenRequest;
import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.dto.UserDto;
import com.brightloop.auth.service.AuthService;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.common.exception.UnauthorizedException;
import com.brightloop.common.service.SequenceGeneratorService;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.security.jwt.JwtTokenProvider;
import com.brightloop.security.jwt.UserPrincipal;
import com.brightloop.user.entity.PasswordResetToken;
import com.brightloop.user.entity.Role;
import com.brightloop.user.entity.User;
import com.brightloop.user.entity.UserSession;
import com.brightloop.user.repo.PasswordResetTokenRepository;
import com.brightloop.user.repo.RoleRepository;
import com.brightloop.user.repo.UserRepository;
import com.brightloop.user.repo.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Value("${jwt.access-token-expiration-ms:86400000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            String accessToken = tokenProvider.generateAccessToken(principal);
            String refreshToken = tokenProvider.generateRefreshToken(principal);

            // Store refresh token hash in DB for rotation and session management
            saveUserSession(user, refreshToken);

            // Set secure HttpOnly cookies
            addAuthCookie(response, "access_token", accessToken, (int) (accessTokenExpirationMs / 1000));
            addAuthCookie(response, "refresh_token", refreshToken, (int) (refreshTokenExpirationMs / 1000));

            UUID customerId = getCustomerIdForUser(user.getId());

            UserDto userDto = UserDto.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().getName())
                    .status(user.getStatus())
                    .emailVerified(user.getEmailVerified())
                    .customerId(customerId)
                    .build();

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresInMs(accessTokenExpirationMs)
                    .user(userDto)
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists");
        }

        // Section 3: "Public registration must always assign CUSTOMER role on the backend. Never allow users to submit their own role."
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        User newUser = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .status("ACTIVE")
                .emailVerified(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // Auto-create customer profile
        String customerCode = sequenceGeneratorService.getNextSequence("CUSTOMER");
        Customer customer = Customer.builder()
                .user(savedUser)
                .customerCode(customerCode)
                .customerType("BUSINESS")
                .companyName(request.getCompanyName() != null ? request.getCompanyName() : request.getFullName())
                .contactName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("ACTIVE")
                .country("India")
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        saveUserSession(savedUser, refreshToken);

        addAuthCookie(response, "access_token", accessToken, (int) (accessTokenExpirationMs / 1000));
        addAuthCookie(response, "refresh_token", refreshToken, (int) (refreshTokenExpirationMs / 1000));

        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getName())
                .status(savedUser.getStatus())
                .emailVerified(savedUser.getEmailVerified())
                .customerId(savedCustomer.getId())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .user(userDto)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletResponse response) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        UserSession session = userSessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Session is revoked or invalid"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Session expired. Please log in again.");
        }

        User user = session.getUser();
        UserPrincipal principal = UserPrincipal.create(user);

        // Rotate tokens
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRefreshToken = tokenProvider.generateRefreshToken(principal);

        // Invalidate old session and create new rotated session
        session.setRevokedAt(Instant.now());
        userSessionRepository.save(session);
        saveUserSession(user, newRefreshToken);

        addAuthCookie(response, "access_token", newAccessToken, (int) (accessTokenExpirationMs / 1000));
        addAuthCookie(response, "refresh_token", newRefreshToken, (int) (refreshTokenExpirationMs / 1000));

        UUID customerId = getCustomerIdForUser(user.getId());

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .customerId(customerId)
                .build();

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .user(userDto)
                .build();
    }

    @Override
    @Transactional
    public void logout(UUID userId, HttpServletResponse response) {
        clearAuthCookie(response, "access_token");
        clearAuthCookie(response, "refresh_token");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            // Prevent user enumeration by silent return
            return;
        }

        User user = userOpt.get();
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {} (raw token for dev: {})", user.getEmail(), rawToken);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String tokenHash = hashToken(request.getToken());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or used password reset token"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID customerId = getCustomerIdForUser(user.getId());

        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .customerId(customerId)
                .build();
    }

    private void saveUserSession(User user, String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(tokenHash)
                .expiresAt(Instant.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();
        userSessionRepository.save(session);
    }

    private UUID getCustomerIdForUser(UUID userId) {
        return customerRepository.findByUserId(userId)
                .map(Customer::getId)
                .orElse(null);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 not available", ex);
        }
    }

    private void addAuthCookie(HttpServletResponse response, String name, String value, int maxAgeSec) {
        if (response == null) return;
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in prod HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        response.addCookie(cookie);
    }

    private void clearAuthCookie(HttpServletResponse response, String name) {
        if (response == null) return;
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
