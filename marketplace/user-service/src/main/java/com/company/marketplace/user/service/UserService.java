package com.company.marketplace.user.service;

import com.company.marketplace.user.config.Topics;
import com.company.marketplace.user.dto.RegisterRequest;
import com.company.marketplace.user.dto.UserEvent;
import com.company.marketplace.user.dto.WalletRequest;
import com.company.marketplace.user.model.UserRow;
import com.company.marketplace.user.model.UserStatusMessage;
import com.company.marketplace.user.outbox.OutBox;
import com.company.marketplace.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RestClient paymentClient;
    private final OutBox outBox;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, RestClient paymentClient, OutBox outBox, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.paymentClient = paymentClient;
        this.outBox = outBox;
        this.objectMapper = objectMapper;
    }

    // регистрация: пользователь + кошелек в payment, либо ничего
    // 1) вставляем пользователя (id уже есть, но транзакция еще не закоммичена)
    // 2) просим payment создать кошелек; не ответил или ошибка - исключение, откат, пользователя нет
    // 3) событие в outbox: notification запомнит email
    @Transactional
    public UserRow register(RegisterRequest request) {
        Optional<Long> created = userRepository.insert(request);
        if (created.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        Long id = created.get();

        try {
            paymentClient.post().uri("/payments/wallets")
                    .body(new WalletRequest(id))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("wallet was not created, rollback user: id={}, cause={}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "payment is unavailable, try later");
        }

        UserEvent event = new UserEvent(id, request.email(), UserStatusMessage.USER_REGISTERED);
        outBox.add(id.toString(), Topics.USER, objectMapper.writeValueAsString(event));
        log.info("register user: id={}, role={}", id, request.role());
        return new UserRow(id, request.name(), request.email(), request.role().name(), false);
    }

    public Optional<UserRow> get(Long id) {
        return userRepository.findById(id);
    }

    // модератор блокирует/разблокирует пользователя
    @Transactional
    public boolean setBlocked(Long id, boolean blocked) {
        return userRepository.updateBlocked(id, blocked) > 0;
    }
}
