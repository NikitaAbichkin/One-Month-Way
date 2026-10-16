package com.company.order;




public record OrderCreatedEvent(String messageId, Long orderId, String email, Integer amount) {
}
