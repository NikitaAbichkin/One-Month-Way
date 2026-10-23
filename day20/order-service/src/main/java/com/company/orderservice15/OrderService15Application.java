package com.company.orderservice15;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderService15Application {

    public static void main(String[] args) {
        SpringApplication.run(OrderService15Application.class, args);
        System.out.println("double to check");
    }
}
