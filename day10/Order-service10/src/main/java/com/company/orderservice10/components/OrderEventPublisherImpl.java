package com.company.orderservice10.Components;

import com.company.orderservice10.OrderEventPublisher;
import com.company.orderservice10.dto.OrderCreatedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisherImpl  implements OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisherImpl.class);
    private  final  KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private  final  String topic;

    public OrderEventPublisherImpl(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                                    @Value("${app.kafka.topic.order-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OrderCreatedEvent event) {

        kafkaTemplate.send(topic,event.customerId(),event)
                .whenComplete((result, ex) ->{
                    if (ex!=null){
                        log.error("Не отправлено: orderId={}", event.orderId(), ex);
                        return;
                    }
                    RecordMetadata metadata= result.getRecordMetadata();
                    log.info("Отправлено: orderId={}, partition={}, offset={}", event.orderId(),metadata.partition(),metadata.offset());

                });
    }
}
