package com.company.marketplace.order.dto;

import com.company.marketplace.order.model.CatalogStatusMessage;

import java.math.BigDecimal;

// то, что catalog присылает в CatalogTopic при создании/изменении товара
public record ProductEvent(Long productId, BigDecimal price, CatalogStatusMessage statusMessage) {
}
