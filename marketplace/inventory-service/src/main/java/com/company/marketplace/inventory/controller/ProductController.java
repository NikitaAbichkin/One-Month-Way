package com.company.marketplace.inventory.controller;

import com.company.marketplace.inventory.dto.StockRequest;
import com.company.marketplace.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final InventoryRepository inventoryRepository;

    public ProductController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // продавец выставляет остаток (цена и название живут в catalog)
    @PutMapping("/{productId}/stock")
    public ResponseEntity<Void> setStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        log.info("set stock: product={}, amount={}", productId, request.amount());
        inventoryRepository.setStock(productId, request.amount());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) {
        return inventoryRepository.findStock(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
