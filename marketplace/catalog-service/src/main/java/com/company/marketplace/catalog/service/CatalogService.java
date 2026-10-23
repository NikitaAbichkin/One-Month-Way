package com.company.marketplace.catalog.service;

import com.company.marketplace.catalog.config.Topics;
import com.company.marketplace.catalog.dto.ProductEvent;
import com.company.marketplace.catalog.dto.ProductRequest;
import com.company.marketplace.catalog.model.CatalogStatusMessage;
import com.company.marketplace.catalog.model.Product;
import com.company.marketplace.catalog.outbox.OutBox;
import com.company.marketplace.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {
    private final ProductRepository productRepository;
    private final OutBox outBox;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    public CatalogService(ProductRepository productRepository, OutBox outBox, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.outBox = outBox;
        this.objectMapper = objectMapper;
    }

    // создание товара + событие в outbox (order узнает цену из CatalogTopic)
    @Transactional
    public Product create(ProductRequest request) {
        Long id = productRepository.insert(request);
        publish(id, request.price());
        log.info("create product: id={}, price={}", id, request.price());
        return new Product(id, request.name(), request.category(), request.price());
    }

    // продавец поменял цену/название: в Kafka уходит новая цена
    @Transactional
    public Optional<Product> update(Long id, ProductRequest request) {
        int updated = productRepository.update(id, request);
        if (updated == 0) return Optional.empty();
        publish(id, request.price());
        log.info("update product: id={}, price={}", id, request.price());
        return Optional.of(new Product(id, request.name(), request.category(), request.price()));
    }

    public Optional<Product> get(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> search(String query, String category) {
        return productRepository.search(query, category);
    }

    private void publish(Long productId, BigDecimal price) {
        ProductEvent event = new ProductEvent(productId, price, CatalogStatusMessage.PRODUCT_UPSERT);
        outBox.add(productId.toString(), Topics.CATALOG, objectMapper.writeValueAsString(event));
    }
}
