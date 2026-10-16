package com.company.gateway16.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TimingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        String requestId = UUID.randomUUID().toString();

        ServerHttpRequest request =
                exchange.getRequest().mutate().header("X-Request-Id", requestId).build();
        ServerWebExchange webExchange = exchange.mutate().request(request).build();
        System.out.println("A pre<" + requestId+">.");


        // что положить перед ответом
        webExchange.getResponse().beforeCommit(()->{
            long end = (System.nanoTime()-start)/1_000_000;
            webExchange.getResponse().getHeaders().add("X-Took", end+"ms");
            return  Mono.empty();
        });

        // после передачи пользователю что сделать
     return chain.filter(webExchange).then(Mono.fromRunnable(()->{
            long end = (System.nanoTime()-start)/1_000_000;
            System.out.println("A post<" + requestId+"> took <"+ end+"> ms");
        }));


    }

    @Override
    public int getOrder() {
        return -10;
    }
}
