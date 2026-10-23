package com.company.marketplace.order.outbox;

import com.company.marketplace.order.model.OutboxMessage;
import com.company.marketplace.order.repository.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OutBox {
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final OutboxTrace outboxTrace;

    public OutBox(KafkaTemplate<String, String> kafkaTemplate, OutboxRepository outboxRepository, OutboxTrace outboxTrace) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.outboxTrace = outboxTrace;
    }

    // пишем в той же транзакции, что и изменение заказа; traceparent - чтобы релей продолжил тот же трейс
    public void add(Long orderId, String topic, String message) {
        outboxRepository.add(orderId, topic, message, outboxTrace.currentTraceparent());
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void checkAndPublishOutBox(){
        List<OutboxMessage> outboxMessages = outboxRepository.findUnsent();
        if (outboxMessages.isEmpty()) return;


        //
        List<CompletableFuture<SendResult<String,String>>> futures =
        outboxMessages.stream().map(message->{
            // send внутри спана с родителем из outbox: traceparent уйдёт заголовком в Kafka
            return outboxTrace.runInStoredTrace(message.traceparent(), "outbox publish",
                    () -> kafkaTemplate.send(message.topic(), message.messageKey(), message.message()));
        }).toList();

        // дожидаемся всех  выполненных
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        outboxRepository.markSent(outboxMessages.stream().map(OutboxMessage::id).toList());
    }
}
