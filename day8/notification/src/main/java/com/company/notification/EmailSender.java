package com.company.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmailSender {
    private static Boolean canGoIfSixSeven = false;
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    public void send(OrderCreatedEvent event) throws IOException {
        if (event.orderId() == 67 ) {
            canGoIfSixSeven =!canGoIfSixSeven;
            if (!canGoIfSixSeven){
                throw new IOException("OU DAAMN");
            }
        }
        if (event.email() == null || event.email().isBlank())
            throw new IllegalArgumentException("email пустой, messageId=" + event.messageId());


        log.info("Письмо отправлено на {}, заказ {}", event.email(), event.orderId());
    }
}
