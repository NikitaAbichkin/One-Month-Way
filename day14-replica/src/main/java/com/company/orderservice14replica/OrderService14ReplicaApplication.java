package com.company.orderservice14replica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderService14ReplicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderService14ReplicaApplication.class, args);
    }
}
