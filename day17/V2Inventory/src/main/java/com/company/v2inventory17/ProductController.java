package com.company.v2inventory17;

import com.company.v2inventory17.Dto.PriceResponse;
import org.springframework.beans.factory.annotation.Value;
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
    private   final  int port;

    public ProductController(JdbcClient jdbc, @Value("${server.port}") Integer port) {
        this.jdbc = jdbc;
        this.port = port;
    }
    @GetMapping("/{productId}/price")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable Long productId) {
        return jdbc.sql("SELECT  price FROM products WHERE product_id = :id")
                .param("id", productId)
                .query(BigDecimal.class)
                .optional()
                .map(p-> ResponseEntity.ok(new PriceResponse(productId, p, port)))
                .orElseGet(()->ResponseEntity.notFound().build());

    }
}
