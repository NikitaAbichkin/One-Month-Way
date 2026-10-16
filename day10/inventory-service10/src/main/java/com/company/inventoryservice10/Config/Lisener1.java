package com.company.inventoryservice10.Config;

import com.company.inventoryservice10.Config.dto.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Lisener1 {

    Logger log = LoggerFactory.getLogger(Lisener1.class);
    private   final ExecutorService pool = Executors.newSingleThreadExecutor();


    //day 10
//    @KafkaListener(topics = "order-created")
//    public  void handle(ConsumerRecord<String, OrderCreatedEvent> record, Acknowledgment acknowledgment){
//        log.info("partition is :{}\n offset is:{}\n key is : {}, value is : {}", record.partition(),  record.offset(), record.key(), record.value());
//        acknowledgment.acknowledge();
//    }

    //day11
    //так как мы скидываем обработку в другой поток, то наш метод быстро завершается
    // мы не можем сделать коммит не обработав их, но такак мы их скидываем другой поток в обработку то
    // то они считаются выполненными и делается  коммит офсета
    // Нужен АВТОКОМИТ, и сделать интервал маленький, чтобы мы взяли 10 обработали и сразу закомитили (хотя они еще не обработанны)

//    @KafkaListener(topics = "order-created")
//    public void handle(ConsumerRecord<String,OrderCreatedEvent> record){
//        log.info("ПОЛУЧЕНО offset={}", record.offset());
//        pool.submit(()->{
//            log.info("START обработка offset={}", record.offset());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {return;}
//
//            if (record.offset()==90) {
//                log.error("КРАШ на offset = 90");
//                Runtime.getRuntime().halt(1);
//            }
//            log.info("DONE  обработка offset={}", record.offset());
//        });
//    }

    // задача, сделать долгую обработку, во время работы уронить приложение  и убедится
    // что коммит не сдалан той записи которую мы не обработали (коммит делаем в конце)

    @KafkaListener(topics = "order-created")
    public void handle(ConsumerRecord<String,OrderCreatedEvent> record, Acknowledgment acknowledgment){
        log.info("ПОЛУЧЕНО offset={}", record.offset());
        try {
            Thread.sleep(300);} catch (InterruptedException e) {return;}
        log.info("DONE  обработка offset={}", record.offset());
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "order-created")
    public void handle(ConsumerRecord<String,OrderCreatedEvent> record){
        log.info("ПОЛУЧЕНО offset={}", record.offset());
        try {
            Thread.sleep(300);} catch (InterruptedException e) {return;}
        log.info("DONE  обработка offset={}", record.offset());
    }





}
