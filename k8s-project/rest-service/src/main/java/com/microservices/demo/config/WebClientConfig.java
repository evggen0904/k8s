package com.microservices.demo.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Setter
@Configuration
@ConfigurationProperties("db-service")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebClientConfig {

    String basePath;
    int connectionTimeout;
    int readTimeout;
    int writeTimeout;
    String userName;
    String password;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(basePath)
                .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(userName, password))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
