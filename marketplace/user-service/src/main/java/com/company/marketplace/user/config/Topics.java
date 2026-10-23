package com.company.marketplace.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Topics {
    public static final String USER = "UserTopic";

    @Bean
    public NewTopic ownTopic() {
        return TopicBuilder.name("UserTopic").partitions(3).replicas(1).build();
    }
}
