package com.company.paymentservice15.Dto;

public record OutboxMessage(
        Long id,
        Long orderId,
        String messageKey,
        String topic,
        String message
) {}
