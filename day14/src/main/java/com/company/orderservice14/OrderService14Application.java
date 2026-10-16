package com.company.orderservice14;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderService14Application {

    public static void main(String[] args) {
        SpringApplication.run(OrderService14Application.class, args);
    }
}
