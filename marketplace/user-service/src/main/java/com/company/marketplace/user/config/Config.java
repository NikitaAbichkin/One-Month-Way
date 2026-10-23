package com.company.marketplace.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class Config {

    // клиент к payment: Builder берем у Spring (с ним трейс идет дальше по заголовку),
    // но ставим таймауты - пока ждем payment, транзакция регистрации открыта
    @Bean
    public RestClient paymentClient(RestClient.Builder builder, @Value("${payment.url:http://localhost:8083}") String paymentUrl) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
        factory.setReadTimeout(Duration.ofSeconds(3));
        return builder.baseUrl(paymentUrl).requestFactory(factory).build();
    }
}
