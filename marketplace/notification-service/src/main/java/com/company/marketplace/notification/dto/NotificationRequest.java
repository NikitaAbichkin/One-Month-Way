package com.company.marketplace.notification.dto;

import com.company.marketplace.notification.model.NotificationStatusMessage;

// команда от order в самом конце саги: заказ завершен или отменен
public record NotificationRequest(Long orderId, Long userId, NotificationStatusMessage statusMessage) {
}
