package com.company.notification;

import java.math.BigDecimal;

public record OrderCreatedEvent(String messageId, Long orderId, String email, Integer amount) {
}
