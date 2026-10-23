package com.company.inventoryservice15.Dto;

public record OrderKafkaRequest(Long orderId, Long productId, Integer amount, String idempotencyKey, InventoryStatusMessage statusMessage) {
}
