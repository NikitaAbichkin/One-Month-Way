package com.company.marketplace.order.dto;

// то, что присылает служба доставки
public record DeliveryWebhookRequest(Long orderId, String status) {
}
