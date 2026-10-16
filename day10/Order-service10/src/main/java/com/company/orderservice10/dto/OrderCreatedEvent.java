package com.company.orderservice10.dto;

public record OrderCreatedEvent (
        String orderId,
        String customerId,
        String status
){}
