package com.brightloop.user.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.user.dto.CreateUserRequest;
import com.brightloop.user.dto.UpdateUserRequest;
import com.brightloop.user.dto.UserResponse;
import com.brightloop.user.entity.Role;
import com.brightloop.user.entity.User;
import com.brightloop.user.repo.RoleRepository;
import com.brightloop.user.repo.UserRepository;
import com.brightloop.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String search, String role, String status, Pageable pageable) {
        Page<User> page = userRepository.searchUsers(search, role, status, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        }

        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role " + request.getRole() + " not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status("ACTIVE")
                .emailVerified(true)
                .build();

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role role = roleRepository.findByName(request.getRole().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
            user.setRole(role);
        }

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        user.setStatus(status.toUpperCase());
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
