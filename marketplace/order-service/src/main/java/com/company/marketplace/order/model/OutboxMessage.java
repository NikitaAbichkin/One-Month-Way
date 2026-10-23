package com.company.marketplace.order.model;

public record OutboxMessage(
        Long id,
        Long orderId,
        String messageKey,
        String topic,
        String message,
        String traceparent
) {}
