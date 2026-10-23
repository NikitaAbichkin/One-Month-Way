package com.company.marketplace.inventory.model;

public record OutboxMessage(
        Long id,
        Long orderId,
        String messageKey,
        String topic,
        String message,
        String traceparent
) {}
