package com.company.marketplace.order.config;

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
    public static final String CATALOG = "CatalogTopic";

    @Bean
    public NewTopic ownTopic() {
        return TopicBuilder.name("OrderTopic").partitions(3).replicas(1).build();
    }

    // читаем цены из CatalogTopic: гарантируем, что топик существует, даже если order поднялся раньше catalog
    @Bean
    public NewTopic catalogTopic() {
        return TopicBuilder.name("CatalogTopic").partitions(3).replicas(1).build();
    }
}
