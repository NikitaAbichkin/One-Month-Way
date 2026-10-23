package com.company.marketplace.notification.repository;

import com.company.marketplace.notification.dto.NotificationRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NotificationRepository {
    private final JdbcClient jdbcClient;

    public NotificationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // 0 - такое письмо уже было (дубль сообщения)
    public int insert(NotificationRequest request, String text, String idempotenceKey) {
        return jdbcClient.sql("""
        insert into notifications (user_id, type, text, order_id, idempotence_key)
        values (:userId, :type, :text, :orderId, :idempotenceKey)
        on conflict do nothing
        """)
                .param("userId", request.userId())
                .param("type", request.statusMessage().name())
                .param("text", text)
                .param("orderId", request.orderId())
                .param("idempotenceKey", idempotenceKey)
                .update();
    }

    public void markSent(String idempotenceKey) {
        jdbcClient.sql("update notifications set send_at = now() where idempotence_key = :idempotenceKey")
                .param("idempotenceKey", idempotenceKey)
                .update();
    }

    // повторное событие просто перезаписывает email
    public void upsertContact(Long userId, String email) {
        jdbcClient.sql("""
        insert into contacts (user_id, email) values (:userId, :email)
        on conflict (user_id) do update set email = :email
        """)
                .param("userId", userId)
                .param("email", email)
                .update();
    }

    public Optional<String> findEmail(Long userId) {
        return jdbcClient.sql("select email from contacts where user_id = :userId")
                .param("userId", userId)
                .query(String.class)
                .optional();
    }
}
