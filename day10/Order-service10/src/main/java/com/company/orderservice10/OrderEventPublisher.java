package com.company.orderservice10;

import com.company.orderservice10.dto.OrderCreatedEvent;

public interface OrderEventPublisher {

    void publish(OrderCreatedEvent event);
}
