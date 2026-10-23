package com.company.marketplace.order.saga;

import com.company.marketplace.order.config.Topics;
import com.company.marketplace.order.dto.InventoryMessage;
import com.company.marketplace.order.dto.OrderPaymentRequest;
import com.company.marketplace.order.dto.NotificationMessage;
import com.company.marketplace.order.dto.OrderResponse;
import com.company.marketplace.order.dto.ProductEvent;
import com.company.marketplace.order.model.*;
import com.company.marketplace.order.outbox.OutBox;
import com.company.marketplace.order.repository.OrderRepository;
import com.company.marketplace.order.repository.PriceRepository;
import com.company.marketplace.order.repository.SagaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

//Какой бы статус нам не пришел и куда бы нам не пришлось мапить строку,
// в любом случае мы
//1) получем ордер  и его параметры
//2) дополняем нужные и отпправляем
// Дто = ордер+доп параметры

@Component
public class Saga {
    private final OrderRepository orderRepository;
    private final SagaRepository sagaRepository;
    private final OutBox outBox;
    private final PriceRepository priceRepository;
    private final ObjectMapper objectMapper;
    private final Counter ordersCounter;
    private static final Logger log = LoggerFactory.getLogger(Saga.class);

    public Saga(OrderRepository orderRepository, SagaRepository sagaRepository, OutBox outBox, PriceRepository priceRepository,
                ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
        this.outBox = outBox;
        this.priceRepository = priceRepository;
        this.objectMapper = objectMapper;
        this.ordersCounter = Counter.builder("orders.created").register(meterRegistry);
    }

    public Optional<OrderView> getOrder(Long id) {
        return orderRepository.findView(id);
    }

    // создание заказа  + оутбокс + Saga
    @Transactional
    public Optional<Long> OrderCreate(OrderResponse orderResponse){
        // цену берем из своей БД (ее обновляет CatalogTopic), в inventory больше не ходим
        BigDecimal price = priceRepository.findPrice(orderResponse.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "price of product " + orderResponse.productId() + " is unknown"));

        Optional<Long> created = orderRepository.insert(orderResponse, price);
       if (created.isEmpty()) return Optional.empty();

       Long id =  created.get();
       sagaRepository.create(id, Step.INVENTORY);

        InventoryMessage reserveInventory = new InventoryMessage(id, orderResponse.productId(),orderResponse.amount(),id+"Creating", StatusMessage.INVENTORY_RESERVE);
        outBox.add(id, Topics.INVENTORY, objectMapper.writeValueAsString(reserveInventory));

        //прибалвяем +1 после коммита
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                      ordersCounter.increment();
                    }
                }
        );
        log.info("create order: product={}, key={}", orderResponse.productId(), orderResponse.idempotenceKey());
        return created;
    }

    // catalog прислал новую цену товара: обновляем локальную копию
    @Transactional
    public void productPriceChanged(ProductEvent event) {
        priceRepository.upsert(event.productId(), event.price());
        log.info("price updated: product={}, price={}", event.productId(), event.price());
    }

    @Transactional
    public void inventoryReserved(Long orderId) {
        OrderRow request = orderRepository.findOrder(orderId);
        BigDecimal totalPrice = request.price().multiply(BigDecimal.valueOf(request.amount()));
        String idempotenceKey = request.id()+"paying";
        OrderPaymentRequest orderPaymentRequest = new OrderPaymentRequest(request.userId(),request.productId(),request.id(),totalPrice,idempotenceKey,StatusMessage.PAYMENT_CREATE);
        String message = objectMapper.writeValueAsString(orderPaymentRequest);

        int updatedStep = sagaRepository.moveWithDeadline(orderId, Step.INVENTORY, Step.PAYMENT);
        if (updatedStep  == 0 ) return;

        outBox.add(orderId, Topics.PAYMENT, message);
    }

    @Transactional
    public void inventoryRejected(Long orderId) {
        int updated = sagaRepository.move(orderId, Step.INVENTORY.name(), Step.CANCELED);
        if (updated == 0 ) return;// дубль
        orderRepository.updateStatus(orderId, "Canceled");

        notifyUser(orderId, orderRepository.findOrder(orderId).userId(), NotificationStatusMessage.ORDER_CANCELED);
    }

    @Transactional
    public void paymentFailed(Long orderId) {
        OrderRow request = orderRepository.findOrder(orderId);
        request = request.addIdempotenceKey(orderId+"compensating");

        int updated = sagaRepository.move(orderId, Step.PAYMENT.name(), Step.CANCELED);
        if(updated == 0 ) return;

        orderRepository.updateStatus(orderId, "Canceled");

        InventoryMessage inventoryMessage = new InventoryMessage(request.id(),request.productId(),request.amount(),request.idempotenceKey(),StatusMessage.INVENTORY_COMPENSATE);
        String message  =  objectMapper.writeValueAsString(inventoryMessage);
        outBox.add(orderId, Topics.INVENTORY, message);

        notifyUser(orderId, request.userId(), NotificationStatusMessage.ORDER_CANCELED);
    }

    @Transactional
    public void paymentSucceeded(Long orderId) {
        int moved = sagaRepository.move(orderId, Step.PAYMENT.name(), Step.DONE);
        if (moved == 0) return;

        orderRepository.updateStatus(orderId, "Done");

        notifyUser(orderId, orderRepository.findOrder(orderId).userId(), NotificationStatusMessage.ORDER_DONE);
    }

    // webhook от службы доставки: оплаченный заказ доставлен
    @Transactional
    public void delivered(Long orderId) {
        int moved = sagaRepository.move(orderId, Step.DONE.name(), Step.DELIVERED);
        if (moved == 0) return; // дубль webhook или заказ не оплачен

        orderRepository.updateStatus(orderId, "Delivered");

        notifyUser(orderId, orderRepository.findOrder(orderId).userId(), NotificationStatusMessage.ORDER_DELIVERED);
    }

    // сообщение в самом конце саги: notification напишет пользователю
    private void notifyUser(Long orderId, Long userId, NotificationStatusMessage status) {
        NotificationMessage message = new NotificationMessage(orderId, userId, status);
        outBox.add(orderId, Topics.NOTIFICATION, objectMapper.writeValueAsString(message));
    }

    // таймаут саги: просроченные заказы отменяем и кладём компенсации в outbox
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void cancelExpiredSagas() {
        List<ExpiredSaga> expired = sagaRepository.findExpired();

        for (ExpiredSaga saga : expired) {
            int moved = sagaRepository.move(saga.orderId(), saga.step(), Step.CANCELED);
            if (moved == 0) continue; // ответ успел прийти

            orderRepository.updateStatus(saga.orderId(), "Canceled");

            InventoryMessage compensate = new InventoryMessage(saga.orderId(), saga.productId(), saga.amount(),
                    saga.orderId() + "timeout", StatusMessage.INVENTORY_COMPENSATE);
            outBox.add(saga.orderId(), Topics.INVENTORY, objectMapper.writeValueAsString(compensate));
            notifyUser(saga.orderId(), saga.userId(), NotificationStatusMessage.ORDER_CANCELED);

            // деньги могли успеть списаться: возврат идемпотентен на стороне payment
            if (Step.PAYMENT.name().equals(saga.step())) {
                OrderPaymentRequest refund = new OrderPaymentRequest(saga.userId(), saga.productId(), saga.orderId(),
                        saga.price().multiply(BigDecimal.valueOf(saga.amount())),
                        saga.orderId() + "refund", StatusMessage.PAYMENT_REFUND);
                outBox.add(saga.orderId(), Topics.PAYMENT, objectMapper.writeValueAsString(refund));
            }
        }
    }
}
