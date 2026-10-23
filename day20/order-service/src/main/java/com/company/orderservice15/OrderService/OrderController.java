package com.company.orderservice15.OrderService;

import com.company.orderservice15.Dto.OrderResponse;
import com.company.orderservice15.Dto.OrderView;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService orderService;
    private final JdbcClient jdbcClient;

    public OrderController(OrderService orderService, JdbcClient jdbcClient) {
        this.orderService = orderService;
        this.jdbcClient = jdbcClient;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable Long id) {
        return jdbcClient.sql("select id, user_id, product_id, price, amount, status from orders where id = :id")
                .param("id", id)
                .query(OrderView.class)
                .optional()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public ResponseEntity<Void> create(@RequestBody OrderResponse request,  @RequestHeader(value = "X-Request-Id", required = false) String requestId ) throws InterruptedException {
//        Thread.sleep(10000);
        orderService.OrderCreate(request);
        System.out.println(requestId);
        return ResponseEntity.accepted().build();
    }
}
