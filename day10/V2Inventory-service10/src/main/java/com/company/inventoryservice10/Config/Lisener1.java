package com.company.inventoryservice10.Config;

import com.company.inventoryservice10.Config.dto.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class Lisener1 {
    Logger log = LoggerFactory.getLogger(Lisener1.class);
    @KafkaListener(topics = "order-created")
    public  void handle(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment acknowledgment){
        log.info("partition is :{}\n offset is:{}\n key is : {}, value is : {}", record.partition(),  record.offset(), record.key(), record.value());
        acknowledgment.acknowledge();
    }
}
