package com.company.mobilebff;

import java.math.BigDecimal;

// только то, что нужно экрану телефона
public record OrderScreen(Long orderId, String status, boolean paid, BigDecimal price) {
}
