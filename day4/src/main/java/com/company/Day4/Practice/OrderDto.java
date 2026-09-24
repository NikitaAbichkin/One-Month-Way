package com.company.Day4.Practice;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Order}
 */
@Value
public class OrderDto implements Serializable {
    Long id;
    Long ProductId;
    Long quantity;
    Status status;
    String Idempotency_Key;
}