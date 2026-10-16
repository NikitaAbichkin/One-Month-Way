package com.company.notificationservice.listener;

import com.company.notificationservice.model.InvalidOrderException;
import com.company.notificationservice.model.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
public class Lisener1 {
    Logger log = LoggerFactory.getLogger(Lisener1.class);

    @KafkaListener(topics = "orders")
    public  void handle(ConsumerRecord<String, OrderEvent> record){
        OrderEvent event = record.value();
        log.info("partition is :{}\n offset is:{}\n key is : {}, value is : {}", record.partition(),  record.offset(), record.key(), record.value());

        // ошибка при  плохом реквесте
        if (event.orderId().startsWith("FAIL"))
            throw  new IllegalStateException("Базза данных недоступна");

        if (event.orderId().startsWith("BAD"))
            throw   new InvalidOrderException("Bad JSON");

    }
}
