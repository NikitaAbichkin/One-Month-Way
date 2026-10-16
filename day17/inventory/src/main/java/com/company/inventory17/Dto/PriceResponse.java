package com.company.inventory17.Dto;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public record PriceResponse(
        Long productId,
        BigDecimal price,
        Integer port
) {
}
