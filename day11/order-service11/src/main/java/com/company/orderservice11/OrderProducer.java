package com.company.orderservice11;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class OrderProducer {
    public static void main(String[] args) {
        SpringApplication.run(OrderProducer.class, args);
        System.out.println("Started on 8081");
    }
}
