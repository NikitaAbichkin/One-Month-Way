package com.company.paymentservice15.Listener;

import com.company.paymentservice15.Dto.*;
import com.company.paymentservice15.Topics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Listener {
    private final  JdbcClient jdbcClient;
    private  final KafkaTemplate<String, String>  kafkaTemplate;
    private  final ObjectMapper objectMapper;
    public Listener(JdbcClient jdbcClient, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    @Transactional
    @KafkaListener(topics = Topics.PAYMENT)
    public  void listener(OrderPaymentRequest request){
        switch (request.statusMessage()){
            case PAYMENT_CREATE -> {

                int inserted = jdbcClient.sql(
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
                if (inserted == 0) return;

                int updatedBalance = jdbcClient.sql("""
                                update  balance set "balance" = "balance"  - :NeedMoney
                where user_id = :userId and balance >= :NeedMoney
                """).param("NeedMoney", request.totalPrice())
                        .param("userId", request.userId())
                        .update();

                if (updatedBalance == 0){
                    PaymentAnswer  answer = new PaymentAnswer(request.orderId(), PaymentStatusMessage.PAYMENT_FAILED);
                    String message = objectMapper.writeValueAsString(answer);
                    jdbcClient.sql(
                                    """
                            insert into outbox(order_id,message_key,topic,message) values (:orderId,:messageKey, :topic,:message)
                                                     """
                            ).param("orderId", request.orderId())
                            .param("messageKey", request.orderId().toString())
                            .param("topic", Topics.ORDER)
                            .param("message", message)
                            .update();

                    jdbcClient.sql("update payments set status = :status where order_id = :orderId")
                            .param("status", PaymentDbStatus.FAILED.name())
                            .param("orderId", request.orderId())
                            .update();

                    return;
                }

                PaymentAnswer paymentAnswer =  new PaymentAnswer(request.orderId(), PaymentStatusMessage.PAYMENT_SUCCEEDED);
                String message = objectMapper.writeValueAsString(paymentAnswer);
                jdbcClient.sql(
                                """
                                insert into outbox(order_id,
                                        message_key,topic,message) values (:orderId,:messageKey,:topic,:message)
                                """
                        ).param("orderId", request.orderId())
                        .param("messageKey", request.orderId().toString())
                        .param("topic", Topics.ORDER)
                        .param("message", message)
                        .update();

            }
            case PAYMENT_REFUND -> {
               int updated = jdbcClient.sql("""
                update  payments  set status = :new 
                where status = :old and order_id = :orderId
                """)
                       .param("old", PaymentDbStatus.DEDUCTED.name())
                       .param("new", PaymentDbStatus.REFUNDED.name())
                       .param("orderId",  request.orderId())
                       .update();

               if( updated ==  0) return;

               jdbcClient.sql("""
                update  balance set "balance" = "balance"+:returnMoney 
                                where user_id  =:userId
                """)
                       .param("returnMoney", request.totalPrice())
                       .param("userId",request.userId())
                       .update();

            }

        }
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public  void checkAndPublishOutBox(){
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
