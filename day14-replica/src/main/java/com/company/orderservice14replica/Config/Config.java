package com.company.orderservice14replica.Config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class Config {
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";

    @Bean
    public DataSource dataSource(){
       return DataSourceBuilder.create()
               .url("jdbc:postgresql://localhost:5437/orders_db")
               .username("orders_user")
               .password("orders_pass")
               .build();

   }

    @Bean
    public JdbcClient jdbcClient(DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(){
        Map<String, Object> map = new HashMap<>();
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        return  new DefaultKafkaProducerFactory<>(map);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String>  producerFactory){
        return   new KafkaTemplate<>(producerFactory);
    }

    @Bean("orders")
    public NewTopic newTopic(){
        return TopicBuilder.name("orders").partitions(3).replicas(1).build();
    }
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS));
    }




}
