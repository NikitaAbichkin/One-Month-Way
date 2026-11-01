package com.company.marketplace.payment.dto;

import java.math.BigDecimal;

public record TopUpRequest(BigDecimal amount) {
}
