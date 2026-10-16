import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Main {
    static void main() throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(9090)
                .intercept(new LoggingInterceptor())
                .addService(new InventoryServiceImp()).build();

        server.start();
        System.out.println("gRPC server started on port 9090");

        server.awaitTermination();
    }
}
