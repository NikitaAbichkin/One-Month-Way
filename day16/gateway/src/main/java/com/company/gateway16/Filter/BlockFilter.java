package com.company.gateway16.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BlockFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String xBlock = exchange.getRequest().getHeaders().getFirst("X-Block");
        if  ("true".equals(xBlock)){
            System.out.println("B blocked");
            exchange.getResponse().setStatusCode(HttpStatus.valueOf(403));
            return exchange.getResponse().setComplete();
        }
        System.out.println("B pre");
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            System.out.println("B post");
        }));

    }

    @Override
    public int getOrder() {
        return -5;
    }
}
