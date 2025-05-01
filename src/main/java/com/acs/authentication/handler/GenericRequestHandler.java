package com.acs.authentication.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.acs.authentication.service.ApiKeyAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Component
public class GenericRequestHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    public <T> Mono<ResponseEntity<String>> handle(T requestDto, HttpMethod method) {
        Map<String, String> queryParams = objectMapper.convertValue(requestDto, new TypeReference<>() {});

        return apiKeyAuthService.callAcsViaKeys(method, queryParams, null)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    ObjectNode error = JsonNodeFactory.instance.objectNode();
                    error.put("error", "ACS Request Failed");
                    error.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(error.toString()));
                });
    }
}
