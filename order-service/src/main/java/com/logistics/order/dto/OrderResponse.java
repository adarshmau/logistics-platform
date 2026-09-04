package com.logistics.order.dto;

import com.logistics.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weightKg;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}