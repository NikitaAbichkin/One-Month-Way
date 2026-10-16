package com.company.notificationservice.config;

import com.company.notificationservice.model.InvalidOrderException;
import com.company.notificationservice.model.OrderEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class kafkaConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:29092";

    @Bean
    public KafkaAdmin kafkaAdmin(){
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS));
    }

    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory(){
        Map<String, Object> config  = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderEvent.class.getName());
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,JacksonJsonDeserializer.class);
        return  new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }
    @Bean
    public KafkaTemplate<String, byte[]> bytesKafkaTemplate(){
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
    }
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String,Object>  template,KafkaTemplate<String, byte[]> bytesTemplate){
        var templates = new LinkedHashMap<Class<?>, KafkaOperations<?,?>>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, template);
        var recoverer = new DeadLetterPublishingRecoverer(templates);

        // хендлер который отлавливает ошибку и думает что с ней делать
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000,2));
        handler.addNotRetryableExceptions(InvalidOrderException.class);
        return  handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory(DefaultErrorHandler  errorHandler){
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
    @Bean("orders")
    public NewTopic orders(){
        return TopicBuilder.name("orders").partitions(3).replicas(1).build();
    }

    @Bean("orders.DLT")
    public NewTopic ordersDLT(){
        return  TopicBuilder.name("orders.DLT").partitions(3).replicas(1).build();
    }


}
