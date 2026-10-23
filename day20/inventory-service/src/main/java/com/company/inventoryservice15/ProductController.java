package com.company.inventoryservice15;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {
    private final JdbcClient jdbc;

    public ProductController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/{productId}/price")
    public ResponseEntity<BigDecimal> getPrice(@PathVariable Long productId) {
        return jdbc.sql("SELECT price FROM products WHERE product_id = :id")
                .param("id", productId)
                .query(BigDecimal.class)
                .optional()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
