package com.company.orderservice15.Dto;

public record OrderResponse(
        Long userId,
        Long productId,
        Integer amount,
        String idempotenceKey
) {
}
