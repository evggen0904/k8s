package com.microservices.demo.service;

import com.microservices.demo.dto.DbDataDto;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataServiceImpl implements DataService {

    WebClient webClient;

    public DataServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<DbDataDto> getData() {
        return webClient.get()
                .uri("/data")
                .retrieve()
                .bodyToMono(DbDataDto.class);
    }

    @Override
    public DbDataDto getDataBlocking() {
        return webClient.get()
                .uri("/data")
                .retrieve()
                .bodyToMono(DbDataDto.class)
                .block();
    }
}
