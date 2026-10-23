package com.company.marketplace.order.dto;

import com.company.marketplace.order.model.NotificationStatusMessage;

// команда в NotificationTopic: сага дошла до конца (заказ оплачен, отменен или доставлен)
public record NotificationMessage(Long orderId, Long userId, NotificationStatusMessage statusMessage) {
}
