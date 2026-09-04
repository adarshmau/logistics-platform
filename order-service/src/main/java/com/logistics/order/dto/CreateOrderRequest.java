package com.logistics.order.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderRequest {


    @NotNull
    private Long userId;

    @NotBlank(message = "pickupAddress is required")
    private String pickupAddress;

    @NotBlank(message = "deliveryAddress is required")
    private String deliveryAddress;

    @Positive(message = "weightKg must be positive")
    private Double weightKg;

}

