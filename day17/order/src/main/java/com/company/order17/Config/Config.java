package com.company.order17.Config;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class Config {

    // Клиент с балансировкой только для inventory. Бин RestClient.Builder с @LoadBalanced
    // нельзя: его подхватывает сам Eureka-клиент и пытается "балансировать" адрес Eureka-сервера.
    @Bean
    RestClient inventoryClient(LoadBalancerClient lb, LoadBalancerRequestFactory factory) {
        return RestClient.builder()
                .baseUrl("http://inventory")
                .requestInterceptor(new LoadBalancerInterceptor(lb, factory))
                .build();
    }
}
