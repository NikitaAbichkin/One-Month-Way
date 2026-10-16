package com.company.order18;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import com.company.order18.Dto.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final InventoryClient inventory;

    public OrderController(InventoryClient inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(inventory.getPrice(productId));
        } catch (BulkheadFullException e) {
            // отбит семафором Bulkhead: в inventory даже не ходили
            log.warn("ОТБИТ Bulkhead: {} | {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (CallNotPermittedException e) {
            // отбит открытым Circuit Breaker
            log.warn("ОТБИТ CircuitBreaker: {} | {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (ResourceAccessException e) {
            // дошли до inventory, но таймаут/нет соединения
            log.warn("ТАЙМАУТ/СЕТЬ: {} | {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (HttpClientErrorException e) {
            log.warn("НЕ НАЙДЕН ТАКОЙ ПРОДУКТ: {} | {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<StockResponse> getStock(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(inventory.getStock(productId));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<String> reserve(@PathVariable Long productId, @RequestParam int amount) {
        return ResponseEntity.ok(inventory.reserve(productId, amount));
    }

    @GetMapping("/{productId}/stock-this")
    public ResponseEntity<StockResponse> getStockThis(@PathVariable Long productId) {
        return ResponseEntity.ok(inventory.getStockViaThis(productId));
    }
}
