package com.company.Day1.Practice.Servieces;

import com.company.Day1.Practice.Entity.Order;
import com.company.Day1.Practice.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private  final  LogAuditService logAuditService;

    @Transactional
    public void createOrder(String name) {
        orderRepo.save(new Order(name));

        try {
            logAuditService.audit("created " + name);
        } catch (RuntimeException e) {}
// по логике у меня метод прошел нормально внутри а значит для менеджера транзаеций все чисто
    }


}
