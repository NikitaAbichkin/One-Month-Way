package com.company.order18;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.company.order18.Dto.StockResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class InventoryClient {
    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient inventory;

    public InventoryClient(RestClient inventoryRestClient) {
        this.inventory = inventoryRestClient;
    }

    @CircuitBreaker(name = "inventory") //пишем имя сервиса к каторому обращаемся
//    @Retry(name = "inventory")
    @Bulkhead(name = "inventory")
    public BigDecimal getPrice(Long productId) {
        long start = System.nanoTime();
        log.info("-> inventory: запрос отправлен, productId={}", productId);
        try {
            return inventory.get()
                    .uri("/inventory/products/{id}/price", productId)
                    .retrieve()
                    .body(BigDecimal.class);

        }
        finally {
            log.info("<- inventory: ответ/ошибка через {} мс, productId={}", (System.nanoTime() - start) / 1_000_000, productId);
        }
    }


    // чтение остатка: сюда нужен fallback "наличие уточняется"
    @CircuitBreaker(name = "inventory")
    @Retry(name = "inventory",fallbackMethod = "getStockFallBack")
    public StockResponse getStock(Long productId) {
        Integer stock = inventory.get()
                .uri("/inventory/products/{id}/stock", productId)
                .retrieve()
                .body(Integer.class);
        return new StockResponse(stock, "OK");
    }

    public StockResponse getStockFallBack(Long productId, Throwable throwable) throws Throwable {
        if(throwable.getClass() == HttpClientErrorException.NotFound.class){
           throw  throwable;
        }
        StockResponse stockResponse = new StockResponse(null,"наличие уточняется ");
        return stockResponse;
    }

    // не делаем fallback
    @CircuitBreaker(name = "inventory")
//    @Retry(name = "inventory")
    public String reserve(Long productId, int amount) {
        return inventory.post()
                .uri("/inventory/products/{id}/reserve?amount={amount}", productId, amount)
                .retrieve()
                .body(String.class);
    }

    public StockResponse getStockViaThis(Long productId) {
        return this.getStock(productId);
    }
}
