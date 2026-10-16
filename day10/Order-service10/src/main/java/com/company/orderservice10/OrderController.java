package com.company.orderservice10;

import com.company.orderservice10.dto.OrderCreatedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderEventPublisher publisher;

    public OrderController(OrderEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody OrderCreatedEvent event) {
        publisher.publish(event);
        return ResponseEntity.accepted().build();

    }
}
