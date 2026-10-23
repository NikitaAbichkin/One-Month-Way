package com.company.marketplace.user.dto;

import com.company.marketplace.user.model.UserRole;

public record RegisterRequest(String name, String email, UserRole role) {
}
