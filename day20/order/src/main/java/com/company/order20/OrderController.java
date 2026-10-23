package com.company.order20;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final InventoryClient inventory;
    private final OrderRepository orders;

    public OrderController(InventoryClient inventory, OrderRepository orders) {
        this.inventory = inventory;
        this.orders = orders;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) {
        return ResponseEntity.ok(inventory.getPrice(productId));
    }

    @PostMapping
    public OrderEntity create(@RequestParam Long productId) {
        OrderEntity order = new OrderEntity();
        order.setProductId(productId);
        return orders.save(order);
    }

    @GetMapping
    public List<OrderEntity> list() {
        return orders.findAll();
    }
}
