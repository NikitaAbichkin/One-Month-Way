package com.company.Day1.Practice.Repository;

import com.company.Day1.Practice.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}