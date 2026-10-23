package com.company.marketplace.payment.dto;

import com.company.marketplace.payment.model.PaymentStatusMessage;

import java.math.BigDecimal;

public record OrderPaymentRequest(
        Long userId,
        Long productId,
        Long orderId,
        BigDecimal totalPrice,
        String idempotenceKey,
        PaymentStatusMessage statusMessage
){
    public OrderPaymentRequest addIdempotenceKey(String idempotenceKey){
        return new OrderPaymentRequest(userId,productId,orderId, totalPrice,idempotenceKey,statusMessage);
    }
}