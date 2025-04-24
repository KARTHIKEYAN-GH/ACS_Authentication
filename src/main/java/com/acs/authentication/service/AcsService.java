package com.acs.authentication.service;

import java.util.Map;

import org.springframework.http.HttpMethod;

import reactor.core.publisher.Mono;

public interface AcsService {

	Mono<String> callAcsApi(HttpMethod post, String command, Map<String, String> queryParams, Object object);

}
