package com.company;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;


@Configuration
public class RestClientConfig {
    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500))
                .build();


        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder().baseUrl("http://localhost:8081")
                .requestFactory(requestFactory)
                .build();
    }
}
