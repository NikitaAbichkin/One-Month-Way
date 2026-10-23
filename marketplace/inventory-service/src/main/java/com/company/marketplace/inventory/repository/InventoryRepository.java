package com.company.marketplace.inventory.repository;

import com.company.marketplace.inventory.dto.OrderKafkaRequest;
import com.company.marketplace.inventory.model.InventoryStatusMessage;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InventoryRepository {
    private final JdbcClient jdbcClient;

    public InventoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // 0 - такой заказ уже был (дубль)
    public int insertOrder(OrderKafkaRequest request) {
        return jdbcClient.sql("""
        insert into inventory_order (order_id, product_id, amount, idempotence_key, status)
        values (:orderId, :productId, :amount, :idempotenceKey, :status)
        on conflict do nothing
        """).param("orderId", request.orderId())
                .param("productId", request.productId())
                .param("amount", request.amount())
                .param("idempotenceKey", request.idempotencyKey())
                .param("status", InventoryStatusMessage.INVENTORY_RESERVED.name())
                .update();
    }

    // 0 - товара нет или остатка не хватает
    public int reserveStock(Long productId, Integer amount) {
        return jdbcClient.sql("""
        update products set amount = amount - :amount
                        where product_id = :productId and amount>= :amount
        """).param("amount", amount)
                .param("productId", productId)
                .update();
    }

    public void updateOrderStatus(Long orderId, InventoryStatusMessage status) {
        jdbcClient.sql("""
        update  inventory_order set status = :status where order_id = :orderId
        """).param("status", status.name())
                .param("orderId", orderId)
                .update();
    }

    // 0 - уже компенсировали или резерва не было
    public int markCompensated(Long orderId) {
        return jdbcClient.sql("""
        update  inventory_order set status = :new
        where order_id = :orderId and status = :old
        """
        ).param("orderId", orderId)
                .param("new", InventoryStatusMessage.INVENTORY_COMPENSATED.name())
                .param("old", InventoryStatusMessage.INVENTORY_RESERVED.name())
                .update();
    }

    public void returnStock(Long productId, Integer amount) {
        jdbcClient.sql("""
        update  products set amount =  amount +:amount where  product_id=:productId
        """)
                .param("amount", amount)
                .param("productId", productId)
                .update();
    }

    // продавец выставляет остаток: товара еще нет - создаем, есть - заменяем
    public void setStock(Long productId, Integer amount) {
        jdbcClient.sql("""
        insert into products (product_id, amount) values (:productId, :amount)
        on conflict (product_id) do update set amount = :amount
        """)
                .param("productId", productId)
                .param("amount", amount)
                .update();
    }

    public Optional<Integer> findStock(Long productId) {
        return jdbcClient.sql("select amount from products where product_id = :productId")
                .param("productId", productId)
                .query(Integer.class)
                .optional();
    }
}
