package com.company.marketplace.inventory.dto;

import com.company.marketplace.inventory.model.InventoryStatusMessage;

public record ReserveStockAnwer(
        Long orderId,
        InventoryStatusMessage statusMessage
) {}
