package com.company.orderservice15.Dto;


import java.math.BigDecimal;

public record OrderRow(Long id, Long userId, Long productId, BigDecimal price, Integer amount,String idempotenceKey ) {

    public BigDecimal totalPrice() {
        return price.multiply(BigDecimal.valueOf(amount));
    }
    public OrderRow addIdempotenceKey (String idempotenceKey){
        return new OrderRow(id,userId,productId,price,amount,idempotenceKey);
    }


}
