package com.company.marketplace.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentService15Application {

    public static void main(String[] args) {
        SpringApplication.run(PaymentService15Application.class, args);
    }
}
