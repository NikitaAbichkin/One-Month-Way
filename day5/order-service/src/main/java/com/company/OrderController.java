package com.company;

import com.company.dto.CreateOrderRequest;
import com.company.order.client.dto.StockResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@RestController
public class OrderController {
    private  final  InventoryClient inventoryClient;

    public OrderController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest createOrderRequest){
        Long productID = createOrderRequest.productId();

        StockResponse answer;
        try {
            answer = inventoryClient.getStock(productID);
        }
        catch (HttpClientErrorException.NotFound e  ){
            return ResponseEntity.status(404).body("Нет товара с таким айди");
        }
        catch (ResourceAccessException e) {
            return  ResponseEntity.status(503).body("Inventory Недоступен");
        }


        if (answer.getAvailable() >= createOrderRequest.quantity()){
            return ResponseEntity.status(201).body("Успешно создано");
        }
        else {
            return ResponseEntity.status(409).body("Нет такого колличества на складе");
        }
   }
}
