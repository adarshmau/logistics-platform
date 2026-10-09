package com.logistics.user.service;

import com.logistics.user.dto.UserRequest;
import com.logistics.user.dto.UserResponse;
import com.logistics.user.exception.ResourceNotFoundException;
import com.logistics.user.mapper.UserMapper;
import com.logistics.user.model.User;
import com.logistics.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Attempted to create user with duplicate email: {}", normalizedEmail);
            throw new IllegalArgumentException("A user with this email already exists");
        }

        request.setEmail(normalizedEmail);
        User saved = userRepository.save(userMapper.toEntity(request));
        log.info("Created new user with id: {}", saved.getId());

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {

        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = findUserOrThrow(id);

        existing.setName(request.getName());
        existing.setEmail(request.getEmail().trim().toLowerCase());
        existing.setPhone(request.getPhone());
        existing.setAddress(request.getAddress());
        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }

        User updated = userRepository.save(existing);
        log.info("Updated user with id: {}", updated.getId());
        return userMapper.toResponse(updated);
    }

    @Override
    public void deleteUser(Long id) {
        User existing = findUserOrThrow(id);
        userRepository.delete(existing);
        log.info("Deleted" +
                "d user with id: {}", id);
    }


    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
    }
}