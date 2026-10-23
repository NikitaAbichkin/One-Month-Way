package com.company.marketplace.order.repository;

import com.company.marketplace.order.model.OutboxMessage;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepository {
    private final JdbcClient jdbcClient;

    public OutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void add(Long orderId, String topic, String message, String traceparent) {
        jdbcClient.sql("""
        insert into outbox(order_id,message_key,topic,message,traceparent) values (:orderId,:messageKey,:topic,:message,:traceparent)
        """)
                .param("traceparent", traceparent)
                .param("orderId", orderId)
                .param("messageKey", orderId.toString())
                .param("topic", topic)
                .param("message", message)
                .update();
    }

    public List<OutboxMessage> findUnsent() {
        return jdbcClient.sql("""
        select id, order_id, message_key, topic, message, traceparent
        from outbox where send_at is null
        order by id FOR  UPDATE  SKIP LOCKED
        """).query(OutboxMessage.class)
                .list();
    }

    public void markSent(List<Long> ids) {
        jdbcClient.sql("""
        update  outbox  set send_at = now() where id in (:ids)
        """)
                .param("ids", ids).update();
    }
}
