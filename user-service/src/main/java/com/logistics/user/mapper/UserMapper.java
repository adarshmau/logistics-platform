package com.logistics.user.mapper;

import com.logistics.user.dto.UserRequest;
import com.logistics.user.dto.UserResponse;
import com.logistics.user.model.User;
import com.logistics.user.model.UserRole;
import org.springframework.stereotype.Component;
//@Component is used for thr making this  class as  bean level and spring will manage this class.
@Component
public class UserMapper {


    public User toEntity(UserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER)
                .build();
    }


    public UserResponse toResponse(User response) {
        return new UserResponse(
                response.getId(),
                response.getName(),
                response.getEmail(),
                response.getPhone(),
                response.getAddress(),
                response.getRole(),
                response.getCreatedAt()
        );
    }


}
