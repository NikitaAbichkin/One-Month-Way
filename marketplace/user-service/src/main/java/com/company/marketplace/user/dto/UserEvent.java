package com.company.marketplace.user.dto;

import com.company.marketplace.user.model.UserStatusMessage;

// то, что уходит в UserTopic: notification по нему запоминает email пользователя
public record UserEvent(Long userId, String email, UserStatusMessage statusMessage) {
}
