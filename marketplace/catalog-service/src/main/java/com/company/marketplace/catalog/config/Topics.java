package com.company.marketplace.catalog.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Topics {
    public static final String CATALOG = "CatalogTopic";

    @Bean
    public NewTopic ownTopic() {
        return TopicBuilder.name("CatalogTopic").partitions(3).replicas(1).build();
    }
}
