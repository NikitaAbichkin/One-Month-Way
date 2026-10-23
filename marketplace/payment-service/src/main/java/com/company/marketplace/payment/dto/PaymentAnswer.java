package com.company.marketplace.payment.dto;

import com.company.marketplace.payment.model.PaymentStatusMessage;

public record PaymentAnswer(
        Long orderId,
        PaymentStatusMessage statusMessage
){
}
