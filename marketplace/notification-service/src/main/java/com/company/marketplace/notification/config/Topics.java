package com.company.marketplace.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Topics {
    public static final String ORDER = "OrderTopic";
    public static final String INVENTORY = "InventoryTopic";
    public static final String PAYMENT = "PaymentTopic";
    public static final String NOTIFICATION = "NotificationTopic";
    public static final String USER = "UserTopic";

    @Bean
    public NewTopic ownTopic() {
        return TopicBuilder.name("NotificationTopic").partitions(3).replicas(1).build();
    }

    // UserTopic читаем (email пользователей): гарантируем, что топик существует
    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("UserTopic").partitions(3).replicas(1).build();
    }
}
