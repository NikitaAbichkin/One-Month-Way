package com.company.marketplace.user.model;

public record UserRow(Long id, String name, String email, String role, Boolean blocked) {
}
