package com.company.orderservice11.dto;

public record OrderCreatedEvent (
        String orderId,
        String customerId,
        String status
){}
