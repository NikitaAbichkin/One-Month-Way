package com.company.v2inventory17.Dto;

import java.math.BigDecimal;

public record PriceResponse(
        Long productId,
        BigDecimal price,
        Integer port
) {
}
