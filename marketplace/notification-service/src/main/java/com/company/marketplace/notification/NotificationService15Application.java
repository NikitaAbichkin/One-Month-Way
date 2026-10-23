package com.company.marketplace.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationService15Application {

    public static void main(String[] args) {
        SpringApplication.run(NotificationService15Application.class, args);
    }
}
