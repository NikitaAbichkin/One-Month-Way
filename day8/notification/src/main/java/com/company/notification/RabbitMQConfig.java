package com.company.notification;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "order-exchange";
    public static final String DLX = "dlx";
    public static final String NOTIFICATION_QUEUE = "notification-queueSWAG";
    public static final String NOTIFICATION_DLQ = "notification-dlq";
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String NOTIFICATION_BINDING_TO_NOTIFICATION_QUEUE = "notification-que-binding";
    public static final String DEAD_LETTER_QUEUE_BINDING_TO_DQL = "dead-letter-queue-bining";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // создали маршрутизатор
    @Bean(ORDER_EXCHANGE)
    public TopicExchange orderExchange(){
        return  new TopicExchange("order-exchange");
    }

    //создали маршрутизатор
    @Bean(DLX)
    public FanoutExchange dlx() {
        return new FanoutExchange("dlx");
    }

    @Bean(NOTIFICATION_QUEUE)
    // создали очередь   которая при возникновении ошибок будет слать в exchange(dlx) с ключем ("x-dead-letter-exchange")
    public Queue notificationQueue(){
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange",DLX)
                .build();
    }

    @Bean(NOTIFICATION_DLQ)
    // очередь которая именуется как (notification-dlq)
    public Queue DeadLetterQueue(){
        return  QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }


    @Bean(NOTIFICATION_BINDING_TO_NOTIFICATION_QUEUE)
    public Binding notificationBinding(@Qualifier(NOTIFICATION_QUEUE) Queue notificationQueue, @Qualifier(ORDER_EXCHANGE) TopicExchange orderExchange){
        return  BindingBuilder.bind(notificationQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_KEY);
    }
    @Bean(DEAD_LETTER_QUEUE_BINDING_TO_DQL)
    public  Binding deadLetterBinding(@Qualifier(NOTIFICATION_DLQ) Queue deadLeetterQueue ,@Qualifier(DLX) FanoutExchange dlx){
        return  BindingBuilder.bind(deadLeetterQueue)
                .to(dlx);
    }


}
