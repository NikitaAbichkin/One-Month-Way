package com.company.marketplace.user.repository;

import com.company.marketplace.user.dto.RegisterRequest;
import com.company.marketplace.user.model.UserRow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // если такой email уже есть - вернется empty (дубль)
    public Optional<Long> insert(RegisterRequest request) {
        return jdbcClient.sql("""
        insert into users (name, email, role) values (:name, :email, :role)
        on conflict (email) do nothing
        returning id
        """)
                .param("name", request.name())
                .param("email", request.email())
                .param("role", request.role().name())
                .query(Long.class)
                .optional();
    }

    public Optional<UserRow> findById(Long id) {
        return jdbcClient.sql("select id, name, email, role, blocked from users where id = :id")
                .param("id", id)
                .query(UserRow.class)
                .optional();
    }

    // 0 - такого пользователя нет
    public int updateBlocked(Long id, boolean blocked) {
        return jdbcClient.sql("update users set blocked = :blocked where id = :id")
                .param("id", id)
                .param("blocked", blocked)
                .update();
    }
}
