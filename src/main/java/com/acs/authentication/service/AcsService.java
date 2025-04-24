package com.acs.authentication.service;

import java.util.Map;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

public interface AcsService {

	Mono<JsonNode> callAcsApi(HttpMethod post, String command, Map<String, String> queryParams, Object object);

}
