package com.company.marketplace.order.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

// локальная копия цен из catalog: так заказ не ходит за ценой по сети
@Repository
public class PriceRepository {
    private final JdbcClient jdbcClient;

    public PriceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // повторное событие просто записывает ту же цену
    public void upsert(Long productId, BigDecimal price) {
        jdbcClient.sql("""
        insert into product_prices (product_id, price) values (:productId, :price)
        on conflict (product_id) do update set price = :price
        """)
                .param("productId", productId)
                .param("price", price)
                .update();
    }

    public Optional<BigDecimal> findPrice(Long productId) {
        return jdbcClient.sql("select price from product_prices where product_id = :productId")
                .param("productId", productId)
                .query(BigDecimal.class)
                .optional();
    }
}
