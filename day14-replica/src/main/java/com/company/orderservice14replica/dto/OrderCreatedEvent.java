package com.company.orderservice14replica.dto;

public record OrderCreatedEvent(
        Long orderId,
        String status
) {}
