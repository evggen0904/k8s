package com.microservices.demo.controller;

import com.microservices.demo.dto.DbDataDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class MyDbController {

    @RequestMapping(
            method = RequestMethod.GET,
            value = {"/data"},
            produces = "application/json"
    )
    public ResponseEntity<DbDataDto> getData() {

        return ResponseEntity.ok(DbDataDto.builder()
                        .id(UUID.randomUUID().toString())
                        .data("Hello from db service")
                .build());
    }
}
