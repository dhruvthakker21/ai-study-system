package com.dhruvthakker.ai_study_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Shared 10MB buffer strategy — used by all WebClients
    private ExchangeStrategies tenMbStrategy() {
        return ExchangeStrategies.builder()
                .codecs(config -> config.defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient youtubeWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .exchangeStrategies(tenMbStrategy())
                .build();
    }

    @Bean
    public WebClient pythonWebClient(@Value("${ai.service.url}") String aiUrl) {
        return WebClient.builder()
                .baseUrl(aiUrl)
                .exchangeStrategies(tenMbStrategy()) // THIS was missing — fixes DataBufferLimitException
                .build();
    }
}