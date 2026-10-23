package com.company.marketplace.order.dto;

import com.company.marketplace.order.model.StatusMessage;

public record InventoryMessage(Long orderId, Long productId, Integer amount, String idempotencyKey, StatusMessage statusMessage){}