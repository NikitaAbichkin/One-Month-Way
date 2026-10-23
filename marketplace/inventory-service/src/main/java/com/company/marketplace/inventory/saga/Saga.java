package com.company.marketplace.inventory.saga;

import com.company.marketplace.inventory.config.Topics;
import com.company.marketplace.inventory.dto.OrderKafkaRequest;
import com.company.marketplace.inventory.dto.ReserveStockAnwer;
import com.company.marketplace.inventory.model.InventoryStatusMessage;
import com.company.marketplace.inventory.outbox.OutBox;
import com.company.marketplace.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class Saga {
    private final InventoryRepository inventoryRepository;
    private final OutBox outBox;
    private final ObjectMapper objectMapper;

    public Saga(InventoryRepository inventoryRepository, OutBox outBox, ObjectMapper objectMapper) {
        this.inventoryRepository = inventoryRepository;
        this.outBox = outBox;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void reserve(OrderKafkaRequest request) {
        int inserted = inventoryRepository.insertOrder(request);
        if (inserted == 0) return; // дубль

        int updated = inventoryRepository.reserveStock(request.productId(), request.amount());

        if (updated == 0 ) {
            ReserveStockAnwer reserveStock = new ReserveStockAnwer(request.orderId(),InventoryStatusMessage.INVENTORY_REJECTED);
            String message = objectMapper.writeValueAsString(reserveStock);
            outBox.add(request.orderId(), Topics.ORDER, message);
            //меняем статус закзаа  на Rejected
            inventoryRepository.updateOrderStatus(request.orderId(), InventoryStatusMessage.INVENTORY_REJECTED);
            return;
        }

        ReserveStockAnwer reserveStock = new ReserveStockAnwer(request.orderId(), InventoryStatusMessage.INVENTORY_RESERVED);
        String message = objectMapper.writeValueAsString(reserveStock);
        outBox.add(request.orderId(), Topics.ORDER, message);
    }

    @Transactional
    public void compensate(OrderKafkaRequest request) {
        int updated = inventoryRepository.markCompensated(request.orderId());

        if (updated == 0) return;

        inventoryRepository.returnStock(request.productId(), request.amount());
    }
}
