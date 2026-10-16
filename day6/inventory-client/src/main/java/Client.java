import com.company.grpc.Inventory;
import com.company.grpc.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Client {
    static void main() throws InterruptedException, NoSuchMethodException {


        // прописываес метаданыне для нашего соединения
        ManagedChannel channel  = ManagedChannelBuilder
                .forAddress("localhost",9090)
                .usePlaintext()
                .build();


        //
        InventoryServiceGrpc.InventoryServiceBlockingStub stub =
                InventoryServiceGrpc.newBlockingStub(channel);

        String[] skus = {"IPHONE-15", "MACBOOK-AIR-M3", "AIRPODS-PRO", "NOKIA-3310"};

        for (String sku : skus){
            Inventory.CheckStockRequest request = Inventory.CheckStockRequest.
                    newBuilder().setSku(sku)
                    .build();
            try {
                Inventory.CheckStockResponse response = stub.
                        withDeadlineAfter(Duration.ofSeconds(1))
                        .checkStock(request);


                System.out.printf("sku = %-16s instock = %-5s quantity = %d%n", response.getSku(),
                        response.getInStock(),
                        response.getQuantity());
            } catch (StatusRuntimeException e) {
               Status.Code code  =  e.getStatus().getCode();

               if ( code == Status.Code.NOT_FOUND){
                   System.out.printf("sku = %-16s NOT FOUND%n", sku);
                   System.out.println(e.getMessage());
               }
               else if ( code == Status.Code.UNAVAILABLE){
                   System.out.println("Inventory service is unavailable, stopping checks");
                    break;
               }
               else {
                   System.out.printf("sku = %-16s error: %s (%s)%n", sku, code, e.getStatus().getDescription());
               }
            }
        }
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
