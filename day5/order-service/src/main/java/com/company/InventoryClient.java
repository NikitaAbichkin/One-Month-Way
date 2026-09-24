package com.company;

import com.company.order.client.dto.StockResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    private  final  RestClient restClient;

    public InventoryClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // метод который может дернуть getStock используя RestClient
    public StockResponse getStock(Long productID){
       return  restClient
                .get().uri("/stock/{productID}", productID)
                .retrieve()
                .body(StockResponse.class);
    }
}
