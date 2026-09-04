package com.logistics.order.service;


import com.logistics.order.client.UserClient;
import com.logistics.order.dto.CreateOrderRequest;
import com.logistics.order.dto.OrderResponse;
import com.logistics.order.dto.UserResponse;
import com.logistics.order.exception.ResourceNotFoundException;
import com.logistics.order.mapper.OrderMapper;
import com.logistics.order.model.Order;
import com.logistics.order.model.OrderStatus;
import com.logistics.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient;
//------------------------------CREATE ORDER-------------------------------------------------------
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

       UserResponse user= userClient.getUserById(request.getUserId());

       log.info("Creating order for verified user:{} ({})" ,user.getName(),user.getEmail());

       Order order = orderMapper.toEntity(request);
       Order save = orderRepository.save(order);

       log.info("Created order with id: {}", save.getId());

       return orderMapper.toResponse(save);
    }
    //-----------------------------getOrderById---------------------------------------------

    @Override
    public  OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus().name().equalsIgnoreCase(status))
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(newStatus);
                    order.setUpdatedAt(LocalDateTime.now());
                    Order updatedOrder = orderRepository.save(order);
                    return orderMapper.toResponse(updatedOrder);})
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public void cancelOrder(Long id) {
        updateStatus(id, OrderStatus.CANCELLED);
    }
}