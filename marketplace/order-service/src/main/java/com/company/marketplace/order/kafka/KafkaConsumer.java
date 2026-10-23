package com.company.marketplace.order.kafka;

import com.company.marketplace.order.config.Topics;
import com.company.marketplace.order.dto.OrderEventKafka;
import com.company.marketplace.order.dto.ProductEvent;
import com.company.marketplace.order.model.StatusMessage;
import com.company.marketplace.order.saga.Saga;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private final Saga saga;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaConsumer.class);

    public KafkaConsumer(Saga saga) {
        this.saga = saga;
    }

    // catalog: товар создан или цена изменилась
    @KafkaListener(topics = Topics.CATALOG)
    public void catalogListener(ProductEvent event){
        log.info("catalog event: product={}, status={}", event.productId(), event.statusMessage());
        saga.productPriceChanged(event);
    }

    @KafkaListener(topics = Topics.ORDER)
    public  void topicListner(OrderEventKafka event){
        log.info("request: orderId={}, status={}", event.orderId(), event.statusMessage());
        switch (event.statusMessage()){
            case StatusMessage.INVENTORY_RESERVED -> saga.inventoryReserved(event.orderId());
            case INVENTORY_REJECTED -> saga.inventoryRejected(event.orderId());
            case PAYMENT_FAILED -> saga.paymentFailed(event.orderId());
            case StatusMessage.PAYMENT_SUCCEEDED -> saga.paymentSucceeded(event.orderId());
        }
    }
}
