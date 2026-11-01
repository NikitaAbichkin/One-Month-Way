package com.company.marketplace.notification.kafka;

import com.company.marketplace.notification.config.Topics;
import com.company.marketplace.notification.dto.NotificationRequest;
import com.company.marketplace.notification.dto.UserEvent;
import com.company.marketplace.notification.saga.Saga;
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

    // команда от order: заказ завершен или отменен
    @KafkaListener(topics = Topics.NOTIFICATION)
    public void notificationListener(NotificationRequest request){
        log.info("traceId:{} orderId:{} event:{}", MDC.get("traceId"), request.orderId(), request.statusMessage());
        saga.notifyUser(request);
    }

    // событие от user-service: новый пользователь и его email
    @KafkaListener(topics = Topics.USER)
    public void userListener(UserEvent event){
        log.info("user event: userId={}, status={}", event.userId(), event.statusMessage());
        saga.saveContact(event);
    }
}
