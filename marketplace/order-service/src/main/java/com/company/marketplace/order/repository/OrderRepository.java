package com.company.marketplace.order.repository;

import com.company.marketplace.order.dto.OrderResponse;
import com.company.marketplace.order.model.OrderRow;
import com.company.marketplace.order.model.OrderView;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class OrderRepository {
    private final JdbcClient jdbcClient;

    public OrderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // если такой idempotenceKey уже есть - вернется empty (дубль)
    public Optional<Long> insert(OrderResponse orderResponse, BigDecimal price) {
        return jdbcClient.sql("""
        insert into orders (user_id, product_id, amount, price, idempotence_key)
        values (:userId, :productId, :amount, :price, :idempotenceKey)
        on conflict (idempotence_key) do nothing
        returning id
        """)
                .param("userId", orderResponse.userId())
                .param("productId", orderResponse.productId())
                .param("amount", orderResponse.amount())
                .param("price", price)
                .param("idempotenceKey", orderResponse.idempotenceKey())
                .query(Long.class)
                .optional();
    }

    public OrderRow findOrder(Long orderId) {
        return jdbcClient.sql("select id, user_id, product_id, price, amount, idempotence_key from orders where id = :id")
                .param("id", orderId)
                .query(OrderRow.class)
                .single();
    }

    public Optional<OrderView> findView(Long id) {
        return jdbcClient.sql("select id, user_id, product_id, price, amount, status from orders where id = :id")
                .param("id", id)
                .query(OrderView.class)
                .optional();
    }

    public void updateStatus(Long orderId, String status) {
        jdbcClient.sql("""
        update  orders set status  = :status where id =:orderId
        """).param("status", status)
                .param("orderId", orderId)
                .update();
    }
}
