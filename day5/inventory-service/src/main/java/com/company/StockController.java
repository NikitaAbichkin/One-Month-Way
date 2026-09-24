package com.company;

import com.company.inventory.StockResponse;
import com.company.inventory.api.StockApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StockController implements StockApi {
Map<Long, Integer > map = Map.of(42L, 10, 7L, 0, 99L, 5);

@Override
    public ResponseEntity<StockResponse> getStock(Long productId)  {
        StockResponse stockResponse = null;
        Integer number;
        if (  (number = map.get(productId))!=null){
            if (productId == 99L ){

                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            stockResponse = new StockResponse();
            stockResponse.setProductId(productId);
            stockResponse.setAvailable(number);
            return  ResponseEntity.status(200).body(stockResponse);
        }
        return ResponseEntity.notFound().build();
    }
}
