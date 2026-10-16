package com.company.notificationservice.model;

public record OrderEvent(
        String orderId,
        String status
) {}
