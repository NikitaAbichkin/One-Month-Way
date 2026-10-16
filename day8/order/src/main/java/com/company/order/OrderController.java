package com.company.order;

import com.company.order.api.OrdersApi;
import com.company.order.model.OrderRequest;
import com.company.order.model.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrdersApi {
    private  final  OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
        String email =  orderRequest.getCustomerEmail();
        Integer amount = orderRequest.getAmount();
        OrderResponse response = orderService.createOrder(email,amount);
        return  ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(Long id) {
        Order order = orderService.getOrder(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderService.toResponse(order));
    }
}
