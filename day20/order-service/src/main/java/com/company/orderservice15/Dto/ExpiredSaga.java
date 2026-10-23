package com.company.orderservice15.Dto;

import java.math.BigDecimal;

public record ExpiredSaga(Long orderId, String step, Long userId, Long productId, Integer amount, BigDecimal price) {
}
