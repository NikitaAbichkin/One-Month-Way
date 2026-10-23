package com.company.marketplace.catalog.controller;

import com.company.marketplace.catalog.dto.ProductRequest;
import com.company.marketplace.catalog.model.Product;
import com.company.marketplace.catalog.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/catalog/products")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
        if (!valid(request)) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(201).body(catalogService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        if (!valid(request)) return ResponseEntity.badRequest().build();
        return catalogService.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return catalogService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // поиск: /catalog/products?q=pho&category=phones
    @GetMapping
    public List<Product> search(@RequestParam(defaultValue = "") String q,
                                @RequestParam(defaultValue = "") String category) {
        return catalogService.search(q, category);
    }

    private boolean valid(ProductRequest request) {
        return request.name() != null && !request.name().isBlank()
                && request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0;
    }
}
