package com.company.inventory18.Controller;

import com.company.inventory18.ChaosState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {
    private final Map<Long, BigDecimal> prices = Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("500.00"),
            3L, new BigDecimal("300.00"));

    private final Map<Long, Integer> stock = new ConcurrentHashMap<>(Map.of(1L, 10, 2L, 5, 3L, 0));

    private final ChaosState chaos;

    public ProductController(ChaosState chaos) {
        this.chaos = chaos;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) throws InterruptedException {
        Thread.sleep(chaos.getDelayMs());
        BigDecimal price = prices.get(productId);
        return price == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(price);
    }

    // GET /inventory/products/{id}/stock: текущий остаток (чтение)
    @GetMapping("/{productId}/stock")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) throws InterruptedException {
        Thread.sleep(chaos.getDelayMs());
        Integer left = stock.get(productId);
        return left == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(left);
    }

    // POST /inventory/products/{id}/reserve?amount=1: списывает остаток (запись)
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<String> reserve(@PathVariable Long productId, @RequestParam int amount) throws InterruptedException {
        Thread.sleep(chaos.getDelayMs());
        if (!stock.containsKey(productId)) {
            return ResponseEntity.status(404).body("product not found");
        }
        boolean[] done = {false};
        stock.computeIfPresent(productId, (k, left) -> {
            if (left >= amount) {
                done[0] = true;
                return left - amount;
            }
            return left;
        });
        return done[0]
                ? ResponseEntity.ok("reserved " + amount + " of product " + productId)
                : ResponseEntity.status(409).body("not enough stock");
    }
}
