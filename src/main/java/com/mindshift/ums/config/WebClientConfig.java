package com.mindshift.ums.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for HTTP calls to external services.
 */
@Configuration
public class WebClientConfig {

    /**
     * Create a WebClient.Builder with common configurations.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)); // 10MB
    }

    /**
     * Default WebClient for general use.
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }

    /**
     * WebClient specifically configured for Kakao API calls.
     */
    @Bean("kakaoWebClient")
    public WebClient kakaoWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "UMS-Service/1.0")
            .build();
    }

    /**
     * WebClient specifically configured for SMS provider API calls.
     */
    @Bean("smsWebClient")
    public WebClient smsWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "UMS-Service/1.0")
            .build();
    }

    /**
     * WebClient specifically configured for FCM API calls.
     */
    @Bean("fcmWebClient")
    public WebClient fcmWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("User-Agent", "UMS-Service/1.0")
            .build();
    }
}