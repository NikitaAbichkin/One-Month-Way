package com.company.marketplace.payment.dto;

import java.math.BigDecimal;

public record WalletResponse(Long userId, BigDecimal balance) {
}
