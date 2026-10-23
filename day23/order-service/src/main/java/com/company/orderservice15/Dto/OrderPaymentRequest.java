package com.company.orderservice15.Dto;

import java.math.BigDecimal;

public record OrderPaymentRequest(
        Long userId,
        Long productId,
        Long orderId,
        BigDecimal totalPrice,
        String idempotenceKey,
        StatusMessage statusMessage
){
    public OrderPaymentRequest addIdempotenceKey(String idempotenceKey){
        return new OrderPaymentRequest(userId,productId,orderId, totalPrice,idempotenceKey, statusMessage);
    }
}
