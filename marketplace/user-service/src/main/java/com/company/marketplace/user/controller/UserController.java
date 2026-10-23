package com.company.marketplace.user.controller;

import com.company.marketplace.user.dto.RegisterRequest;
import com.company.marketplace.user.model.UserRow;
import com.company.marketplace.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRow> register(@RequestBody RegisterRequest request) {
        if (request.name() == null || request.name().isBlank()
                || request.email() == null || !request.email().contains("@")
                || request.role() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(userService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRow> get(@PathVariable Long id) {
        return userService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable Long id) {
        return userService.setBlocked(id, true) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id) {
        return userService.setBlocked(id, false) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
