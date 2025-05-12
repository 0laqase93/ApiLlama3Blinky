package com.blinky.apillama3blinky.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ia.url}")
    private String iaUrl;

    @Bean
    public WebClient iaWebClient() {
        return WebClient.builder()
                .baseUrl(iaUrl)
                .build();
    }
}
