package com.company.order;

import com.company.order.model.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private  final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public OrderResponse createOrder(String customerEmail, Integer amount) {
        Order order = new Order(idGenerator.incrementAndGet(), customerEmail, amount, "NEW");
        orders.put(order.getId(), order);
        OrderResponse orderResponse = toResponse(order);
        OrderCreatedEvent orderCreatedEvent = toEvent(orderResponse);
        orderEventPublisher.publisherOrderCreated(orderCreatedEvent);
        return orderResponse;
    }

    public Order getOrder(Long id) {
        return orders.get(id);
    }


    public OrderResponse toResponse(Order order) {
        return new OrderResponse()
                .id(order.getId())
                .customerEmail(order.getCustomerEmail())
                .amount(order.getAmount())
                .status(OrderResponse.StatusEnum.fromValue(order.getStatus()));
    }

    public OrderCreatedEvent toEvent(OrderResponse orderResponse) {
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                orderResponse.getId(),
                orderResponse.getCustomerEmail(),
                orderResponse.getAmount()
        );
    }
}
