package com.company.marketplace.catalog.dto;

import com.company.marketplace.catalog.model.CatalogStatusMessage;

import java.math.BigDecimal;

// то, что уходит в CatalogTopic: order по нему обновляет свою таблицу цен
public record ProductEvent(Long productId, BigDecimal price, CatalogStatusMessage statusMessage) {
}
