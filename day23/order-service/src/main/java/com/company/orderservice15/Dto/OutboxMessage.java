package com.company.orderservice15.Dto;

public record OutboxMessage(
        Long id,
        Long orderId,
        String messageKey,
        String topic,
        String message,
        String traceparent
) {}
