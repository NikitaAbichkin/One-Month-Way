package com.company.orderservice15.Orkerstration;

import com.company.orderservice15.Dto.*;
import com.company.orderservice15.Topics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Orkerstration {
    private   final KafkaTemplate<String,String>  kafkaTemplate;
    private  final JdbcClient jdbcClient;

    private  final ObjectMapper objectMapper;

    public Orkerstration(KafkaTemplate<String, String> kafkaTemplate, JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    // таймаут саги: просроченные заказы отменяем и кладём компенсации в outbox
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void cancelExpiredSagas() {
        List<ExpiredSaga> expired = jdbcClient.sql("""
        select s.order_id, s.step, o.user_id, o.product_id, o.amount, o.price
        from saga_orders s join orders o on o.id = s.order_id
        where s.step in (:steps) and s.deadline < now()
        order by s.deadline
        for update of s skip locked
        """)
                .param("steps", List.of(Step.INVENTORY.name(), Step.PAYMENT.name()))
                .query(ExpiredSaga.class)
                .list();

        for (ExpiredSaga saga : expired) {
            int moved = jdbcClient.sql("""
            update saga_orders set step = :step
            where order_id = :orderId and step = :from
            """)
                    .param("step", Step.CANCELED.name())
                    .param("from", saga.step())
                    .param("orderId", saga.orderId())
                    .update();
            if (moved == 0) continue; // ответ успел прийти

            jdbcClient.sql("update orders set status = 'Canceled' where id = :orderId")
                    .param("orderId", saga.orderId())
                    .update();

            InventoryMessage compensate = new InventoryMessage(saga.orderId(), saga.productId(), saga.amount(),
                    saga.orderId() + "timeout", StatusMessage.INVENTORY_COMPENSATE);
            addToOutbox(saga.orderId(), Topics.INVENTORY, objectMapper.writeValueAsString(compensate));

            // деньги могли успеть списаться: возврат идемпотентен на стороне payment
            if (Step.PAYMENT.name().equals(saga.step())) {
                OrderPaymentRequest refund = new OrderPaymentRequest(saga.userId(), saga.productId(), saga.orderId(),
                        saga.price().multiply(BigDecimal.valueOf(saga.amount())),
                        saga.orderId() + "refund", StatusMessage.PAYMENT_REFUND);
                addToOutbox(saga.orderId(), Topics.PAYMENT, objectMapper.writeValueAsString(refund));
            }
        }
    }

    private void addToOutbox(Long orderId, String topic, String message) {
        jdbcClient.sql("""
        insert into outbox(order_id,message_key,topic,message) values (:orderId,:messageKey,:topic,:message)
        """)
                .param("orderId", orderId)
                .param("messageKey", orderId.toString())
                .param("topic", topic)
                .param("message", message)
                .update();
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void checkAndPublishOutBox(){
       List<OutboxMessage> outboxMessages = jdbcClient.sql("""
        select id, order_id, message_key, topic, message
        from outbox where send_at is null
        order by id FOR  UPDATE  SKIP LOCKED 
        """).query(OutboxMessage.class)
               .list();
        if (outboxMessages.isEmpty()) return;

        List<CompletableFuture<SendResult<String,String>>> futures =
        outboxMessages.stream().map(message->{
            return kafkaTemplate.send(message.topic(),message.messageKey(),message.message());
        }).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        jdbcClient.sql("""
        update  outbox  set send_at = now() where id in (:ids)
        """)
                .param("ids", outboxMessages.stream().map(OutboxMessage::id).toList()).update();
    }


}
