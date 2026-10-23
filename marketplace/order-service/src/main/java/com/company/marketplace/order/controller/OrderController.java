package com.company.marketplace.order.controller;

import com.company.marketplace.order.dto.OrderResponse;
import com.company.marketplace.order.model.OrderView;
import com.company.marketplace.order.saga.Saga;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class OrderController {
    private final Saga saga;

    public OrderController(Saga saga) {
        this.saga = saga;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable Long id) {
        return saga.getOrder(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public ResponseEntity<Void> create(@RequestBody OrderResponse request,  @RequestHeader(value = "X-Request-Id", required = false) String requestId ) throws InterruptedException {
//        Thread.sleep(10000);
        // id нового заказа - в заголовке Location (повторный запрос с тем же ключом: заголовка нет)
        Optional<Long> id = saga.OrderCreate(request);
        System.out.println(requestId);
        return id.map(orderId -> ResponseEntity.accepted().header("Location", "/orders/" + orderId).<Void>build())
                .orElseGet(() -> ResponseEntity.accepted().build());
    }
}
