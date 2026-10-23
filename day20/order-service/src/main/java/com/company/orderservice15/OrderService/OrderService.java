package com.company.orderservice15.OrderService;

import com.company.orderservice15.Dto.*;
import com.company.orderservice15.Topics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;




@Service
public class OrderService {
    private final JdbcClient jdbcClient;
    private final RestClient inventoryClient;
    private  final ObjectMapper objectMapper;

    public OrderService(JdbcClient jdbcClient, ObjectMapper objectMapper,
                        @Value("${inventory.url:http://localhost:8082}") String inventoryUrl) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.inventoryClient = RestClient.builder().baseUrl(inventoryUrl).build();
    }

    // создание заказа  + оутбокс + Saga
    @Transactional
    public  void OrderCreate(OrderResponse orderResponse){
        BigDecimal price =  inventoryClient.get().uri("/inventory/products/{productId}/price",orderResponse.productId())
                        .retrieve()
                                .body(BigDecimal.class);

        Optional<Long> created = jdbcClient.sql("""
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
       if (created.isEmpty()) return;

       Long id =  created.get();
       jdbcClient.sql("""
        insert into saga_orders(order_id,step,deadline) values (:orderId, :step, now() + interval '30 seconds')
        """)
               .param("orderId", id)
               .param("step", Step.INVENTORY.name())
               .update();

        InventoryMessage reserveInventory = new InventoryMessage(id, orderResponse.productId(),orderResponse.amount(),id+"Creating", StatusMessage.INVENTORY_RESERVE);
       jdbcClient.sql("""
        insert into outbox (order_id,message_key,topic,message) values (:orderId,:messageKey,:topic,:message)
        
        """).param("orderId", id)
               .param("messageKey", id.toString())
               .param("topic", Topics.INVENTORY)
               .param("message", objectMapper.writeValueAsString(reserveInventory))
               .update();
    }

}
