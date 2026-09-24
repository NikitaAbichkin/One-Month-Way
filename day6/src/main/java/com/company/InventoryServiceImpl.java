package com.company;

import com.company.grpc.Inventory;
import com.company.grpc.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;

public class InventoryServiceImpl  extends InventoryServiceGrpc.InventoryServiceImplBase {
    @Override
    public  void checkStock(Inventory.CheckStockRequest request,
                            StreamObserver<Inventory.CheckStockResponse> responseObserver
                            ){

        String sku = request.getSku();

        Inventory.CheckStockResponse response = Inventory.CheckStockResponse.newBuilder()
                .setSku(sku)
                .setQuantity(42)
                .setInStock(true).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }


}
