package com.company.marketplace.notification.dto;

import com.company.marketplace.notification.model.UserStatusMessage;

// событие из UserTopic: запоминаем email пользователя
public record UserEvent(Long userId, String email, UserStatusMessage statusMessage) {
}
