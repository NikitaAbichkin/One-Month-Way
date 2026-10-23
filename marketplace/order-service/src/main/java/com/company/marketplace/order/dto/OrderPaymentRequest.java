package com.company.marketplace.order.dto;

import com.company.marketplace.order.model.StatusMessage;

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
