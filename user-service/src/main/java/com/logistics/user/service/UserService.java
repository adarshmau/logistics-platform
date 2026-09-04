package com.logistics.user.service;


import com.logistics.user.dto.UserRequest;
import com.logistics.user.dto.UserResponse;
import com.logistics.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}
