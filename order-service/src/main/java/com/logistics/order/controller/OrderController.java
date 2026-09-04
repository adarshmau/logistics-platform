package com.logistics.order.controller;

import com.logistics.order.dto.CreateOrderRequest;
import com.logistics.order.dto.OrderResponse;
import com.logistics.order.dto.UserResponse;
import com.logistics.order.model.OrderStatus;
import com.logistics.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid  @RequestBody CreateOrderRequest request) {

        OrderResponse  created = orderService.createOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long id) {

        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> findAllOrdersByUserId(@PathVariable Long userId) {

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> findAllOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id
                                       , @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        // call orderService.cancelOrder(id), then return ResponseEntity.noContent().build()
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

}
