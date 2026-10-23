package com.company.marketplace.user.model;

public record OutboxMessage(
        Long id,
        String messageKey,
        String topic,
        String message,
        String traceparent
) {}
