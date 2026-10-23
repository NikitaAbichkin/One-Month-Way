package com.company.marketplace.order.dto;

import com.company.marketplace.order.model.StatusMessage;

import java.math.BigDecimal;

public record OrderEventKafka(
   Long orderId,
   StatusMessage statusMessage
) {}
