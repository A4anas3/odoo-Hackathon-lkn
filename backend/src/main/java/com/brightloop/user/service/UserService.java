package com.brightloop.user.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.user.dto.CreateUserRequest;
import com.brightloop.user.dto.UpdateUserRequest;
import com.brightloop.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    PageResponse<UserResponse> listUsers(String search, String role, String status, Pageable pageable);
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    UserResponse updateUserStatus(UUID id, String status);
}
