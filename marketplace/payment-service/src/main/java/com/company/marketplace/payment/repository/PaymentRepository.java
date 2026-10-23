package com.company.marketplace.payment.repository;

import com.company.marketplace.payment.dto.OrderPaymentRequest;
import com.company.marketplace.payment.model.PaymentDbStatus;
import com.company.marketplace.payment.model.PaymentRow;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class PaymentRepository {
    private final JdbcClient jdbcClient;

    public PaymentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // 0 - такой платеж уже был (дубль)
    public int insertPayment(OrderPaymentRequest request) {
        return jdbcClient.sql(
                        """
        insert  into payments (user_id,  order_id ,  total_price,
                        idempotence_key, status)
        values (:userId, :orderId,
                        :totalPrice,:idempotenceKey,:status)   on conflict do nothing
        """)
                .param( "userId",  request.userId())
                .param("orderId",  request.orderId())
                .param("totalPrice" , request.totalPrice())
                .param("idempotenceKey",request.idempotenceKey())
                .param("status",PaymentDbStatus.DEDUCTED.name())
                .update();
    }

    // 0 - денег не хватило (или кошелька нет)
    public int deductBalance(Long userId, BigDecimal needMoney) {
        return jdbcClient.sql("""
                        update  balance set "balance" = "balance"  - :NeedMoney
        where user_id = :userId and balance >= :NeedMoney
        """).param("NeedMoney", needMoney)
                .param("userId", userId)
                .update();
    }

    public void updateStatus(Long orderId, PaymentDbStatus status) {
        jdbcClient.sql("update payments set status = :status where order_id = :orderId")
                .param("status", status.name())
                .param("orderId", orderId)
                .update();
    }

    // 0 - уже вернули или списания не было
    public int markRefunded(Long orderId) {
        return jdbcClient.sql("""
        update  payments  set status = :new
        where status = :old and order_id = :orderId
        """)
                .param("old", PaymentDbStatus.DEDUCTED.name())
                .param("new", PaymentDbStatus.REFUNDED.name())
                .param("orderId",  orderId)
                .update();
    }

    public void returnBalance(Long userId, BigDecimal returnMoney) {
        jdbcClient.sql("""
        update  balance set "balance" = "balance"+:returnMoney
                        where user_id  =:userId
        """)
                .param("returnMoney", returnMoney)
                .param("userId", userId)
                .update();
    }

    // кошелек пользователя при регистрации; true - создали, false - уже был
    public boolean createWallet(Long userId, BigDecimal initialBalance) {
        return jdbcClient.sql("""
        insert into balance (user_id, balance) values (:userId, :balance) on conflict do nothing
        """)
                .param("userId", userId)
                .param("balance", initialBalance)
                .update() > 0;
    }

    public Optional<PaymentRow> findByOrder(Long orderId) {
        return jdbcClient.sql("select id, user_id, order_id, total_price, idempotence_key, status from payments where order_id = :orderId")
                .param("orderId", orderId)
                .query(PaymentRow.class)
                .optional();
    }

    public Optional<PaymentRow> findById(Long id) {
        return jdbcClient.sql("select id, user_id, order_id, total_price, idempotence_key, status from payments where id = :id")
                .param("id", id)
                .query(PaymentRow.class)
                .optional();
    }
}
