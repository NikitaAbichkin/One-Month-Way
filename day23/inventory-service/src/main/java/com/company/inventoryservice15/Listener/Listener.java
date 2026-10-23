package com.company.inventoryservice15.Listener;

import com.company.inventoryservice15.Dto.*;
import com.company.inventoryservice15.OutboxTrace;
import com.company.inventoryservice15.Topics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.web.servlet.function.RequestPredicates.param;

@Component
public class Listener {
    private  final KafkaTemplate<String,String> kafkaTemplate;
    private  final JdbcClient jdbcClient;
    private  final  ObjectMapper objectMapper;
    private final OutboxTrace outboxTrace; private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Listener.class);

    public Listener(KafkaTemplate<String, String> kafkaTemplate, JdbcClient jdbcClient, ObjectMapper objectMapper, OutboxTrace outboxTrace) {
        this.kafkaTemplate = kafkaTemplate;
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.outboxTrace = outboxTrace;
    }

    @KafkaListener(topics = Topics.INVENTORY)
    @Transactional
    public void listner(OrderKafkaRequest request){
        log.info("Inventory request: orderId={}, status={}", request.orderId(), request.statusMessage());
        switch (request.statusMessage()){
            case INVENTORY_RESERVE -> {
                int inserted = jdbcClient.sql("""
        insert into inventory_order (order_id, product_id, amount, idempotence_key, status)
        values (:orderId, :productId, :amount, :idempotenceKey, :status)
        on conflict do nothing
        """).param("orderId", request.orderId())
                        .param("productId", request.productId())
                        .param("amount", request.amount())
                        .param("idempotenceKey", request.idempotencyKey())
                        .param("status", InventoryStatusMessage.INVENTORY_RESERVED.name())
                        .update();
                if (inserted == 0) return; // дубль


                int updated = jdbcClient.sql("""
                update products set amount = amount - :amount 
                                where product_id = :productId and amount>= :amount
                """).param("amount", request.amount())
                                .param("productId", request.productId())
                                .update();

                        if (updated == 0 ) {
                            ReserveStockAnwer reserveStock = new ReserveStockAnwer(request.orderId(),InventoryStatusMessage.INVENTORY_REJECTED);
                            String message = objectMapper.writeValueAsString(reserveStock);
                            jdbcClient.sql("""
                             insert  into outbox (order_id,  message_key, topic , message, traceparent) values (:orderId, :messageKey,:topic,:message, :traceparent)
                            """).param("orderId", request.orderId())
                                    .param("messageKey", request.orderId().toString())
                                    .param("topic", Topics.ORDER)
                                    .param("message", message)
                                    .param("traceparent", outboxTrace.currentTraceparent())
                                    .update();
                            //меняем статус закзаа  на Rejected
                            jdbcClient.sql("""
                            update  inventory_order set status = :status where order_id = :orderId
                            """).param("status", InventoryStatusMessage.INVENTORY_REJECTED.name())
                                    .param("orderId", request.orderId())
                                    .update();
                            return;
                        }

                ReserveStockAnwer reserveStock = new ReserveStockAnwer(request.orderId(), InventoryStatusMessage.INVENTORY_RESERVED);
                        String message = objectMapper.writeValueAsString(reserveStock);

                        jdbcClient.sql("""
                insert  into outbox (order_id,  message_key, topic , message, traceparent) values (:orderId, :messageKey,:topic,:message, :traceparent)
                """
                                ).param("orderId",request.orderId())
                                .param("messageKey", request.orderId().toString())
                                .param("topic", Topics.ORDER)
                                .param("message",message)
                                .param("traceparent", outboxTrace.currentTraceparent())
                                .update();

            }
            case INVENTORY_COMPENSATE -> {
                int updated = jdbcClient.sql("""
                    update  inventory_order set status = :new 
                    where order_id = :orderId and status = :old 
                    """
                ).param("orderId", request.orderId())
                        .param("new", InventoryStatusMessage.INVENTORY_COMPENSATED.name())
                        .param("old", InventoryStatusMessage.INVENTORY_RESERVED.name())
                        .update();

                if (updated == 0) return;

                jdbcClient.sql("""
                update  products set amount =  amount +:amount where  product_id=:productId
                """)
                        .param("amount", request.amount())
                        .param("productId", request.productId())
                        .update();
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public  void checkAndPublishOutBox(){
        List<OutboxMessage> outboxMessages = jdbcClient.sql("""
        select id, order_id, message_key, topic, message, traceparent
        from outbox where send_at is null
        order by id FOR  UPDATE  SKIP LOCKED 
        """).query(OutboxMessage.class)
                .list();
        if (outboxMessages.isEmpty()) return;

//        try { Thread.sleep(40_000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }



        List<CompletableFuture<SendResult<String,String>>> futures =
                outboxMessages.stream().map(message->{
                    return  outboxTrace.runInStoredTrace(message.traceparent(), "Inventory Publish OutBox",()->
                        kafkaTemplate.send(message.topic(), message.messageKey(), message.message())
                    );
                }).toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        jdbcClient.sql("""
        update  outbox  set send_at = now() where id in (:ids)
        """)
                .param("ids", outboxMessages.stream().map(OutboxMessage::id).toList()).update();

    }
}
