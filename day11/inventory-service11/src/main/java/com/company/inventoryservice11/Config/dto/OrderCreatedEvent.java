package com.company.inventoryservice11.Config.dto;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String status
){}
