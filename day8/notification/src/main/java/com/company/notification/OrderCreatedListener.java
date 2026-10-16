package com.company.notification;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderCreatedListener {
    private  final  EmailSender emailSender;

    public OrderCreatedListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }


    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event, Channel channel,
    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        try {
            emailSender.send(event);
            channel.basicAck(deliveryTag,false);
        }
        catch (IOException e) {
            try {
                channel.basicNack(deliveryTag,  false, true);
            } catch (IOException ex) {}
        }
        catch (RuntimeException e) {
            try {
                channel.basicNack(deliveryTag,  false, false);
            } catch (IOException ex) {}
        }

    }
}
