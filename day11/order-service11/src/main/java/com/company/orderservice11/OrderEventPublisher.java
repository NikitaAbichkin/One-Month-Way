package com.company.orderservice11;

import com.company.orderservice11.dto.OrderCreatedEvent;

public interface OrderEventPublisher {

    void publish(OrderCreatedEvent event);
}
