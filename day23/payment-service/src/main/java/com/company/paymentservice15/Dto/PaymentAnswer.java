package com.company.paymentservice15.Dto;

public record PaymentAnswer(
        Long orderId,
        PaymentStatusMessage statusMessage
){
}
