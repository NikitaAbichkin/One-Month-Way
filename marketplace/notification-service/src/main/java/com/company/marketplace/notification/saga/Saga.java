package com.company.marketplace.notification.saga;

import com.company.marketplace.notification.dto.NotificationRequest;
import com.company.marketplace.notification.dto.UserEvent;
import com.company.marketplace.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class Saga {
    private final NotificationRepository notificationRepository;
    private static final Logger log = LoggerFactory.getLogger(Saga.class);

    public Saga(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // user-service зарегистрировал пользователя: запоминаем, на какой email ему писать
    @Transactional
    public void saveContact(UserEvent event) {
        notificationRepository.upsertContact(event.userId(), event.email());
        log.info("contact saved: user={}", event.userId());
    }

    // order дошел до конца саги: "отправляем" письмо (по заданию - просто строка в логе)
    @Transactional
    public void notifyUser(NotificationRequest request) {
        String text = switch (request.statusMessage()) {
            case ORDER_DONE -> "заказ " + request.orderId() + " оформлен и оплачен";
            case ORDER_CANCELED -> "заказ " + request.orderId() + " отменен";
            case ORDER_DELIVERED -> "заказ " + request.orderId() + " доставлен";
        };
        String idempotenceKey = request.orderId() + "-" + request.statusMessage().name();

        int inserted = notificationRepository.insert(request, text, idempotenceKey);
        if (inserted == 0) return; // дубль

        Optional<String> email = notificationRepository.findEmail(request.userId());
        if (email.isEmpty()) {
            log.warn("email of user {} is unknown, notification skipped: {}", request.userId(), text);
            return;
        }

        log.info("Письмо отправлено на {}: {}", email.get(), text);
        notificationRepository.markSent(idempotenceKey);
    }
}
