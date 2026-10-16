package com.company.orderservice14.dto;

public record OrderCreatedEvent(
        Long orderId,
        String status
) {}
