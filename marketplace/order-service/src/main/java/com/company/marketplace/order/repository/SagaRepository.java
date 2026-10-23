package com.company.marketplace.order.repository;

import com.company.marketplace.order.model.ExpiredSaga;
import com.company.marketplace.order.model.Step;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SagaRepository {
    private final JdbcClient jdbcClient;

    public SagaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // step фиксируется ДО выполнения шага (в той же транзакции, что и отправка команды)
    public void create(Long orderId, Step step) {
        jdbcClient.sql("""
        insert into saga_orders(order_id,step,deadline) values (:orderId, :step, now() + interval '30 seconds')
        """)
                .param("orderId", orderId)
                .param("step", step.name())
                .update();
    }

    // 0 - шаг уже не from (дубль сообщения или ответ пришел после таймаута)
    public int move(Long orderId, String from, Step to) {
        return jdbcClient.sql("""
        update saga_orders set step = :step
        where order_id = :orderId and step = :from
        """)
                .param("step", to.name())
                .param("orderId", orderId)
                .param("from", from)
                .update();
    }

    // то же самое, но для нового шага заводим новый дедлайн
    public int moveWithDeadline(Long orderId, Step from, Step to) {
        return jdbcClient.sql("""
        update saga_orders set step = :step, deadline = now() + interval '30 seconds'
        where  order_id = :orderId and  step = :from
        """)
                .param("step", to.name())
                .param("orderId", orderId)
                .param("from", from.name())
                .update();
    }

    public List<ExpiredSaga> findExpired() {
        return jdbcClient.sql("""
        select s.order_id, s.step, o.user_id, o.product_id, o.amount, o.price
        from saga_orders s join orders o on o.id = s.order_id
        where s.step in (:steps) and s.deadline < now()
        order by s.deadline
        for update of s skip locked
        """)
                .param("steps", List.of(Step.INVENTORY.name(), Step.PAYMENT.name()))
                .query(ExpiredSaga.class)
                .list();
    }
}
