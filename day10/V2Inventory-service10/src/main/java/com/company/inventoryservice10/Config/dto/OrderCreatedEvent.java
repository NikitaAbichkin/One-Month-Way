package com.company.inventoryservice10.Config.dto;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String status
){}
