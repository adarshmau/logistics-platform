package com.logistics.order.mapper;

import com.logistics.order.dto.CreateOrderRequest;
import com.logistics.order.dto.OrderResponse;
import com.logistics.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        return Order.builder()
                .userId(request.getUserId())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .weightKg(request.getWeightKg())
                .build();
    }

    public  OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getWeightKg(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}