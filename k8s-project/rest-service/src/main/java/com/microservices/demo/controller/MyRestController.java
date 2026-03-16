package com.microservices.demo.controller;

import com.microservices.demo.dto.DbDataDto;
import com.microservices.demo.service.DataService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyRestController {

    DataService dataService;

    public MyRestController(DataService dataService) {
        this.dataService = dataService;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = {"/inner/test"},
            produces = "application/json"
    )
    public Mono<ResponseEntity<String>> test() {

        return Mono.just(ResponseEntity.ok("Hello from inner method"));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = {"/db/test"},
            produces = "application/json"
    )
    public Mono<ResponseEntity<DbDataDto>> getExternalData() {
        return dataService.getData()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
