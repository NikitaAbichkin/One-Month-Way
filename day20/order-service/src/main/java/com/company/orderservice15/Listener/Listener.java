package com.company.orderservice15.Listener;

import com.company.orderservice15.Dto.*;
import com.company.orderservice15.Topics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

//Какой бы статус нам не пришел и куда бы нам не пришлось мапить строку,
// в любом случае мы
//1) получем ордер  и его параметры
//2) дополняем нужные и отпправляем
// Дто = ордер+доп параметры


@Component
public class Listener {
    private  final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;
    public Listener(JdbcClient jdbcClient, ObjectMapper objectMapper) {

        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }


    public OrderRow findOrder(Long orderId) {
        return jdbcClient.sql("select id, user_id, product_id, price, amount, idempotence_key from orders where id = :id")
                .param("id", orderId)
                .query(OrderRow.class)
                .single();
    }


    @KafkaListener(topics = Topics.ORDER)
    @Transactional
    public  void topicListner(OrderEventKafka event){
        switch (event.statusMessage()){
            case StatusMessage.INVENTORY_RESERVED -> {
                OrderRow request = findOrder(event.orderId());
                BigDecimal totalPrice = request.price().multiply(BigDecimal.valueOf(request.amount()));
                String idempotenceKey = request.id()+"paying";
                OrderPaymentRequest orderPaymentRequest = new OrderPaymentRequest(request.userId(),request.productId(),request.id(),totalPrice,idempotenceKey,StatusMessage.PAYMENT_CREATE);
                String message = objectMapper.writeValueAsString(orderPaymentRequest);


                int updatedStep = jdbcClient.sql(
                """
                update saga_orders set step = :step, deadline = now() + interval '30 seconds'
                where  order_id = :orderId and  step = :from
                """
                ).param("step", Step.PAYMENT.name())
                        .param("orderId", event.orderId())
                        .param("from",Step.INVENTORY.name())
                        .update();
                if (updatedStep  == 0 ) return;

                jdbcClient.sql(
                                """
                                insert into outbox(order_id,message_key,topic,message) values (:orderId,:messageKey,:topic,:message)
                                """
                        ).param("orderId", event.orderId())
                        .param("messageKey", event.orderId().toString())
                        .param("topic", Topics.PAYMENT)
                        .param("message", message)
                        .update();

            }
            case INVENTORY_REJECTED -> {

                int updated = jdbcClient.sql("""
                update saga_orders set step = :step
                where order_id = :orderId and step = :from
                """).param("step", Step.CANCELED.name())
                        .param("from", Step.INVENTORY.name())
                        .param("orderId", event.orderId())
                        .update();
                if (updated == 0 ) return;// дубль
                jdbcClient.sql("""
                update  orders set status  = 'Canceled' where id =:orderId
                """).param("orderId", event.orderId())
                                .update();


            }
            case PAYMENT_FAILED -> {
                OrderRow request = findOrder(event.orderId());
                request = request.addIdempotenceKey(event.orderId()+"compensating");

                int updated = jdbcClient.sql("""
                update saga_orders set step = :step
                 where order_id = :orderId and step = :from
                """).param("step", Step.CANCELED.name())
                        .param("from", Step.PAYMENT.name())
                        .param("orderId", event.orderId())
                        .update();
                if(updated == 0 ) return;

                jdbcClient.sql("""
                update  orders set status  = 'Canceled' where id =:orderId
                """).param("orderId", event.orderId())
                                .update();



                InventoryMessage inventoryMessage = new InventoryMessage(request.id(),request.productId(),request.amount(),request.idempotenceKey(),StatusMessage.INVENTORY_COMPENSATE);
                String message  =  objectMapper.writeValueAsString(inventoryMessage);
                jdbcClient.sql(
                                """
                                insert into outbox(order_id,message_key,topic,message) values (:orderId,:messageKey,:topic,:message)     
                                """
                        ).param("orderId", event.orderId())
                        .param("messageKey", event.orderId().toString())
                        .param("topic", Topics.INVENTORY)
                        .param("message", message)
                        .update();

            }
            case StatusMessage.PAYMENT_SUCCEEDED ->{

                int moved = jdbcClient.sql("""
                update saga_orders set step = :step
                    where order_id = :orderId and step = :from
                 """)
                        .param("step", Step.DONE.name())
                        .param("from", Step.PAYMENT.name())
                        .param("orderId", event.orderId())
                        .update();
                if (moved == 0) return;

                jdbcClient.sql("""
                update  orders set status  = 'Done' where id =:orderId
                """).param("orderId", event.orderId())
                        .update();

            }


        }
    }
}
