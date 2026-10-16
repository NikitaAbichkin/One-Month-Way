package com.company.inventory19.Controller;

import com.company.inventory19.ChaosState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {
    private final Map<Long, BigDecimal> prices = Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("500.00"),
            3L, new BigDecimal("300.00"));

    private final ChaosState chaos;

    public ProductController(ChaosState chaos) {
        this.chaos = chaos;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) throws InterruptedException {
        Thread.sleep(chaos.getDelayMs());
        if (ThreadLocalRandom.current().nextInt(100) < chaos.getFailRate()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        BigDecimal price = prices.get(productId);
        return price == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(price);
    }
}
