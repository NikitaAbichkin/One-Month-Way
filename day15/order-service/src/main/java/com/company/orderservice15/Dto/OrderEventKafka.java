package com.company.orderservice15.Dto;

import java.math.BigDecimal;

public record OrderEventKafka(
   Long orderId,
   StatusMessage statusMessage
) {}
