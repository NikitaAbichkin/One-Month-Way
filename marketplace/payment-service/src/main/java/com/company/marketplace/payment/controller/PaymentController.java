package com.company.marketplace.payment.controller;

import com.company.marketplace.payment.dto.WalletRequest;
import com.company.marketplace.payment.model.PaymentRow;
import com.company.marketplace.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentRepository paymentRepository;
    private final BigDecimal initialBalance;

    public PaymentController(PaymentRepository paymentRepository,
                             @Value("${payment.wallet.initial-balance:1000}") BigDecimal initialBalance) {
        this.paymentRepository = paymentRepository;
        this.initialBalance = initialBalance;
    }

    // user-service дергает при регистрации: нет ответа - пользователь не создается
    // идемпотентно: повторный вызов с тем же userId ничего не меняет
    @PostMapping("/wallets")
    public ResponseEntity<Void> createWallet(@RequestBody WalletRequest request) {
        boolean created = paymentRepository.createWallet(request.userId(), initialBalance);
        log.info("create wallet: user={}, created={}", request.userId(), created);
        return created ? ResponseEntity.status(201).build() : ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentRow> getByOrder(@PathVariable Long orderId) {
        return paymentRepository.findByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentRow> getPayment(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
