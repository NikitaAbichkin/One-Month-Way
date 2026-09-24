import com.company.grpc.Inventory;
import com.company.grpc.InventoryServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryServiceImp  extends
        InventoryServiceGrpc.InventoryServiceImplBase {

    // имитация базы данных
    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "IPHONE-15", 3,
            "MACBOOK-AIR-M3", 0,
            "AIRPODS-PRO", 12
    ));


    @Override
    public  void checkStock (Inventory.CheckStockRequest request,
                             StreamObserver<Inventory.CheckStockResponse> responseObserver){


        String sku = request.getSku();
        boolean hasThisSku = stock.containsKey(sku);


        // обрабатываем вараиант когда у нас нету такого ключа
        if (!hasThisSku){
            StatusRuntimeException notFoundSkuException = Status.NOT_FOUND.withDescription("There is not this sku ")
                    .asRuntimeException();

            responseObserver.onError(notFoundSkuException);
            return;
        }

        int quantity = stock.getOrDefault(sku, 0 );

        Inventory.CheckStockResponse response = Inventory.CheckStockResponse.newBuilder()
                .setSku(sku)
                .setQuantity( quantity)
                .setInStock(quantity==0?false:true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }
}

