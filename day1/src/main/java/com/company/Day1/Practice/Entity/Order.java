package com.company.Day1.Practice.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    Long id;
    String name;

    protected Order() {
    }

    public Order(String name){
        this.name = name;
    }
}