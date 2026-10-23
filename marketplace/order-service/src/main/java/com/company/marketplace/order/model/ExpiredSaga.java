package com.company.marketplace.order.model;

import java.math.BigDecimal;

public record ExpiredSaga(Long orderId, String step, Long userId, Long productId, Integer amount, BigDecimal price) {
}
