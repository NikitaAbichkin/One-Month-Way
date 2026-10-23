package com.company.marketplace.catalog.model;

public record OutboxMessage(
        Long id,
        String messageKey,
        String topic,
        String message,
        String traceparent
) {}
