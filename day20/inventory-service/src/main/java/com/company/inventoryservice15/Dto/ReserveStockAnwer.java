package com.company.inventoryservice15.Dto;

public record ReserveStockAnwer(
        Long orderId,
        InventoryStatusMessage statusMessage
) {}
