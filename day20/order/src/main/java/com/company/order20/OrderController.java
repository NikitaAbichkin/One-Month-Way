package com.company.order20;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final InventoryClient inventory;

    public OrderController(InventoryClient inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) {
        return ResponseEntity.ok(inventory.getPrice(productId));
    }
}
