package com.company.marketplace.payment.model;

import java.math.BigDecimal;

public record PaymentRow(Long id, Long userId, Long orderId, BigDecimal totalPrice, String idempotenceKey, String status) {
}
