package com.company.paymentservice15;

import com.company.paymentservice15.Dto.PaymentRow;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final JdbcClient jdbcClient;

    public PaymentController(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentRow> getByOrder(@PathVariable Long orderId) {
        return jdbcClient.sql("select id, user_id, order_id, total_price, idempotence_key, status from payments where order_id = :orderId")
                .param("orderId", orderId)
                .query(PaymentRow.class)
                .optional()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentRow> getPayment(@PathVariable Long id) {
        return jdbcClient.sql("select id, user_id, order_id, total_price, idempotence_key, status from payments where id = :id")
                .param("id", id)
                .query(PaymentRow.class)
                .optional()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
