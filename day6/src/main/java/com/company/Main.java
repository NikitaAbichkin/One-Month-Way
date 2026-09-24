package com.company;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(9090)
                .addService(new InventoryServiceImpl()).build();

        server.start();
        System.out.println("gRPC server started on port 9090");

        server.awaitTermination();
    }
}
