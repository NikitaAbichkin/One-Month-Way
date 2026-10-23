package com.company.orderservice15;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class Config {

    @Bean
    public StringJacksonJsonMessageConverter jsonConverter() {
        return new StringJacksonJsonMessageConverter();
    }

    // сообщение, которое не удалось обработать, уходит в OrderTopic-dlt, а не теряется
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 3));
        handler.addNotRetryableExceptions(BadSqlGrammarException.class);
        return handler;
    }

    // DLT с тем же числом партиций, что и основной топик (recoverer пишет в ту же партицию)
    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name("OrderTopic-dlt").partitions(3).replicas(1).build();
    }
}
