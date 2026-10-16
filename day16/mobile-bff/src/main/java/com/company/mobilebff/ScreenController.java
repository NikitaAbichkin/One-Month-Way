package com.company.mobilebff;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;

@RestController
public class ScreenController {
    // то, что отдают сервисы (берём только нужные поля)
    record OrderDto(Long id, Long productId, String status) {}
    record PaymentDto(String status) {}

    // один фабричный объект с таймаутами на все клиенты
    private static final SimpleClientHttpRequestFactory FACTORY = new SimpleClientHttpRequestFactory();
    static {
        FACTORY.setConnectTimeout(Duration.ofMillis(500));
        FACTORY.setReadTimeout(Duration.ofSeconds(2));
    }

    private static RestClient createClient(String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).requestFactory(FACTORY).build();
    }

    private final RestClient orders = createClient("http://localhost:8081");
    private final RestClient payments = createClient("http://localhost:8083");
    private final RestClient inventory = createClient("http://localhost:8082");

    @GetMapping("/orders/{id}/screen")
    public ResponseEntity<OrderScreen> screen(@PathVariable Long id) {
        // 1. order: заказ по id
        OrderDto order;
        try {
            order = orders.get().uri("/orders/{id}", id).retrieve().body(OrderDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }

        // 2. payment: статус оплаты этого заказа (платежа может ещё не быть)
        boolean paid;
        try {
            PaymentDto payment = payments.get().uri("/payments/order/{orderId}", id).retrieve().body(PaymentDto.class);
            paid = payment != null && "DEDUCTED".equals(payment.status());
        } catch (HttpClientErrorException.NotFound e) {
            paid = false;
        }

        // 3. inventory: цена товара из заказа
        BigDecimal price = inventory.get()
                .uri("/inventory/products/{productId}/price", order.productId())
                .retrieve()
                .body(BigDecimal.class);

        return ResponseEntity.ok(new OrderScreen(order.id(), order.status(), paid, price));
    }
}
