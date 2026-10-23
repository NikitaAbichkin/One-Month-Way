package com.company.inventoryservice15;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryService15Application {

    public static void main(String[] args) {
        SpringApplication.run(InventoryService15Application.class, args);
    }
}
