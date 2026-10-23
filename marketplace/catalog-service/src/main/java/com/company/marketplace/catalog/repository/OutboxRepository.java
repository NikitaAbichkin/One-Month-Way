package com.company.marketplace.catalog.repository;

import com.company.marketplace.catalog.model.OutboxMessage;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepository {
    private final JdbcClient jdbcClient;

    public OutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void add(String messageKey, String topic, String message, String traceparent) {
        jdbcClient.sql("""
        insert into outbox(message_key,topic,message,traceparent) values (:messageKey,:topic,:message,:traceparent)
        """)
                .param("messageKey", messageKey)
                .param("topic", topic)
                .param("message", message)
                .param("traceparent", traceparent)
                .update();
    }

    public List<OutboxMessage> findUnsent() {
        return jdbcClient.sql("""
        select id, message_key, topic, message, traceparent
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
