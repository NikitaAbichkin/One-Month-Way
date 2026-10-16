package com.company.orderservice15.Dto;

public record InventoryMessage(Long orderId, Long productId, Integer amount, String idempotencyKey, StatusMessage statusMessage){}