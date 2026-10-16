package com.company.orderservice14;


import com.company.orderservice14.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {
    private  final JdbcClient jdbcClient;
    private  final String topic = "orders";
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(OrderService.class);
    private  final KafkaTemplate<String,String> kafkaTemplate;


    private record Order( Long orderId, String status) {};
    public record OrdersOutbox(Long id, Long aggregateId, String topic, String payload) {}


    public OrderService(JdbcClient jdbcClient, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }


    public OrderResponse createOrder(String status ){
        Long id = jdbcClient.sql("""
        insert   into  orders(status) values (:status) RETURNING id 
        """)
                .param("status", status)
                .query(Long.class)
                .single();


        if (id %2 == 0 )  throw new RuntimeException("test");

        String payload = objectMapper.writeValueAsString(new Order(id,status));

        jdbcClient.sql(
        """ 
            insert into  outbox (aggregate_id, topic,payload) values (:aggregate_id, :topic,:payload )
        """).param("aggregate_id",  id)
                .param("topic", topic)
                .param("payload", payload)
                .update();


        return new OrderResponse(id, status);
    }

    // jobs


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void pollingPublisher() {

        // получаем все события которые не оптравленные
        List<OrdersOutbox> events = jdbcClient.sql("""
            SELECT id, aggregate_id, topic, payload
            FROM outbox
            WHERE sent_at IS NULL
            ORDER BY id
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """)
                .query(OrdersOutbox.class)
                .list();

        if (events.isEmpty()) return;

        // дальше: отправить в Kafka и сделать UPDATE sent_at
        List<CompletableFuture<SendResult<String, String>>> futures =  events.stream().map(object ->{
         return      kafkaTemplate.send(object.topic(),object.aggregateId().toString(), object.payload());
        }).toList();


        //дожидаемся всех ответов
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        try {
            Thread.sleep(Duration.ofSeconds(4));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // фиксируем в бд и  ставим дату отправки = сейчас
        jdbcClient.sql("""
        UPDATE outbox SET sent_at = now() where id in (:ids)
        """).param("ids", events.stream().map(OrdersOutbox::id).toList()).update();

        for (OrdersOutbox ordersOutbox : events){
            log.info("Мы обработали заказ с айди = " + ordersOutbox.aggregateId);
        }
    }



    }