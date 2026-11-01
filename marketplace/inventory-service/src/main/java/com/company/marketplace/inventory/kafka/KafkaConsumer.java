package com.company.marketplace.inventory.kafka;

import com.company.marketplace.inventory.config.Topics;
import com.company.marketplace.inventory.dto.OrderKafkaRequest;
import com.company.marketplace.inventory.saga.Saga;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private final Saga saga;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaConsumer.class);

    public KafkaConsumer(Saga saga) {
        this.saga = saga;
    }

    @KafkaListener(topics = Topics.INVENTORY)
    public void listner(OrderKafkaRequest request){
        log.info("traceId:{} orderId:{} event:{}", MDC.get("traceId"), request.orderId(), request.statusMessage());
        switch (request.statusMessage()){
            case INVENTORY_RESERVE -> saga.reserve(request);
            case INVENTORY_COMPENSATE -> saga.compensate(request);
        }
    }
}
