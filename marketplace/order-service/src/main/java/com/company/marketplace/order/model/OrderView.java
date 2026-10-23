package com.company.marketplace.order.model;

import java.math.BigDecimal;

public record OrderView(Long id, Long userId, Long productId, BigDecimal price, Integer amount, String status) {
}
