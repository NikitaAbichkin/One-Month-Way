package com.company.marketplace.payment.saga;

import com.company.marketplace.payment.config.Topics;
import com.company.marketplace.payment.dto.OrderPaymentRequest;
import com.company.marketplace.payment.dto.PaymentAnswer;
import com.company.marketplace.payment.model.PaymentDbStatus;
import com.company.marketplace.payment.model.PaymentStatusMessage;
import com.company.marketplace.payment.outbox.OutBox;
import com.company.marketplace.payment.repository.PaymentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class Saga {
    private final PaymentRepository paymentRepository;
    private final OutBox outBox;
    private final ObjectMapper objectMapper;

    public Saga(PaymentRepository paymentRepository, OutBox outBox, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.outBox = outBox;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void create(OrderPaymentRequest request) {
        int inserted = paymentRepository.insertPayment(request);
        if (inserted == 0) return;

        int updatedBalance = paymentRepository.deductBalance(request.userId(), request.totalPrice());

        if (updatedBalance == 0){
            PaymentAnswer  answer = new PaymentAnswer(request.orderId(), PaymentStatusMessage.PAYMENT_FAILED);
            String message = objectMapper.writeValueAsString(answer);
            outBox.add(request.orderId(), Topics.ORDER, message);

            paymentRepository.updateStatus(request.orderId(), PaymentDbStatus.FAILED);

            return;
        }

        PaymentAnswer paymentAnswer =  new PaymentAnswer(request.orderId(), PaymentStatusMessage.PAYMENT_SUCCEEDED);
        String message = objectMapper.writeValueAsString(paymentAnswer);
        outBox.add(request.orderId(), Topics.ORDER, message);
    }

    @Transactional
    public void refund(OrderPaymentRequest request) {
        int updated = paymentRepository.markRefunded(request.orderId());

        if( updated ==  0) return;

        paymentRepository.returnBalance(request.userId(), request.totalPrice());
    }
}
