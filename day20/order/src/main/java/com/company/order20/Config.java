package com.company.order20;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class Config {

    // connect timeout 1 с, read timeout 2 с
    @Bean
    RestClient inventoryRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .requestFactory(factory)
                .build();
    }
}
