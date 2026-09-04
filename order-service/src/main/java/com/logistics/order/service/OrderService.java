package com.logistics.order.service;

import com.logistics.order.dto.CreateOrderRequest;
import com.logistics.order.dto.OrderResponse;
import com.logistics.order.model.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByUserId(Long userId);
    List<OrderResponse> getOrdersByStatus(String status);
    OrderResponse updateStatus(Long id, OrderStatus newStatus );
    void cancelOrder(Long id);
}
