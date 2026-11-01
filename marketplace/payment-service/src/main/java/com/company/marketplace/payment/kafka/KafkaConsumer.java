package com.company.marketplace.payment.kafka;

import com.company.marketplace.payment.config.Topics;
import com.company.marketplace.payment.dto.OrderPaymentRequest;
import com.company.marketplace.payment.saga.Saga;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaConsumer.class);
    private final Saga saga;

    public KafkaConsumer(Saga saga) {
        this.saga = saga;
    }

    @KafkaListener(topics = Topics.PAYMENT)
    public  void listener(OrderPaymentRequest request){
        log.info("traceId:{} orderId:{} event:{}", MDC.get("traceId"), request.orderId(), request.statusMessage());
        switch (request.statusMessage()){
            case PAYMENT_CREATE -> saga.create(request);
            case PAYMENT_REFUND -> saga.refund(request);
        }
    }
}
