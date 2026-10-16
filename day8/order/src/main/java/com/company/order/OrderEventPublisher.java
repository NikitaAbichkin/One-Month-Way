package com.company.order;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final  String EXCHANGE  = "order-exchange";
    private static final String ROUTING_KEY = "order.created";
    private  final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publisherOrderCreated(OrderCreatedEvent  orderCreatedEvent){
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, orderCreatedEvent);
    }

}
