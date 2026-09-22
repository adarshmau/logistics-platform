package com.logistics.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
