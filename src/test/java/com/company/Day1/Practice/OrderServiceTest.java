package com.company.Day1.Practice;

import com.company.Day1.Practice.Repository.AuditRepository;
import com.company.Day1.Practice.Repository.OrderRepository;
import com.company.Day1.Practice.Servieces.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService service;
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    AuditRepository auditRepo;

    @Test
    void auditSurvivesRollback() {
        service.createOrder("test");

        assertThat(orderRepo.count()).isEqualTo(1);   // заказ откатился
//        assertThat(auditRepo.count()).isEqualTo(1);   // аудит остался
    }
}