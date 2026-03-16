package com.microservices.demo.service;

import com.microservices.demo.dto.DbDataDto;
import reactor.core.publisher.Mono;

public interface DataService {
    Mono<DbDataDto> getData();

    DbDataDto getDataBlocking();
}
