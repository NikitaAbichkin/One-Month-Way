package com.company.order22;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class InventoryClient {
    private final RestClient inventory;
    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    public InventoryClient(RestClient inventoryRestClient) {
        this.inventory = inventoryRestClient;
    }

    @Retry(name = "inventory")
    @CircuitBreaker(name = "inventory")
    public BigDecimal getPrice(Long productId) {
        log.info("Вызов inventory");
        return inventory.get()
                .uri("/inventory/products/{id}/price", productId)
                .retrieve()
                .body(BigDecimal.class);
    }
}
