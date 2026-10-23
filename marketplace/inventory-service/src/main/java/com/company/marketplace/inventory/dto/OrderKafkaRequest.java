package com.company.marketplace.inventory.dto;

import com.company.marketplace.inventory.model.InventoryStatusMessage;

public record OrderKafkaRequest(Long orderId, Long productId, Integer amount, String idempotencyKey, InventoryStatusMessage statusMessage) {
}
