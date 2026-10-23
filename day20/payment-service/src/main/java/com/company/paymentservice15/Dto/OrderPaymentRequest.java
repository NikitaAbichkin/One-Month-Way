package com.company.paymentservice15.Dto;

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