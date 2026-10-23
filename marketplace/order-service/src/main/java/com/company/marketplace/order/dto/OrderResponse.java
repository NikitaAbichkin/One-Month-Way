package com.company.marketplace.order.dto;

public record OrderResponse(
        Long userId,
        Long productId,
        Integer amount,
        String idempotenceKey
) {
}
