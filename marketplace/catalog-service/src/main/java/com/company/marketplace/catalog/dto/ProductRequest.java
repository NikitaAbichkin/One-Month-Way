package com.company.marketplace.catalog.dto;

import java.math.BigDecimal;

public record ProductRequest(String name, String category, BigDecimal price) {
}
