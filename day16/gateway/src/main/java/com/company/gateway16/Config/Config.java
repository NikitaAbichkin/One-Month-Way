package com.company.gateway16.Config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class Config {

    @Bean("userKeyResolver")
    public KeyResolver userKeyResolver(){
        return  (exchange) -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.justOrEmpty(userId);
        };
    }
}
