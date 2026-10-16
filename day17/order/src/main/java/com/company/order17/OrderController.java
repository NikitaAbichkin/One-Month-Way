package com.company.order17;

import com.company.order17.Dto.PriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private  final RestClient inventory;
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    public OrderController(RestClient inventoryClient) {
        this.inventory = inventoryClient;
    }

    // TODO: вызвать inventory и вернуть его ответ (с портом)
    @GetMapping("/{productId}/price")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable Long productId) {
        try {
            PriceResponse body = inventory.get()
                    .uri("/inventory/products/{id}/price", productId)
                    .retrieve()
                    .body(PriceResponse.class);

            log.info("inventory ответил: port={}, productId={}", body.port(), productId);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.warn("inventory ошибка: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
