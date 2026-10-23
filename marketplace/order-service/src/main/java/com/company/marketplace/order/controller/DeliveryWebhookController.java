package com.company.marketplace.order.controller;

import com.company.marketplace.order.dto.DeliveryWebhookRequest;
import com.company.marketplace.order.saga.Saga;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// webhook от внешней службы доставки: "заказ доставлен"
@RestController
@RequestMapping("/webhooks")
public class DeliveryWebhookController {
    private final Saga saga;

    public DeliveryWebhookController(Saga saga) {
        this.saga = saga;
    }

    @PostMapping("/delivery")
    public ResponseEntity<Void> delivery(@RequestBody DeliveryWebhookRequest request) {
        if (request.orderId() == null || !"DELIVERED".equals(request.status())) {
            return ResponseEntity.badRequest().build();
        }
        if (saga.getOrder(request.orderId()).isEmpty()) return ResponseEntity.notFound().build();

        // повторный webhook безопасен: шаг уже сдвинут, saga ничего не сделает
        saga.delivered(request.orderId());
        return ResponseEntity.ok().build();
    }
}
