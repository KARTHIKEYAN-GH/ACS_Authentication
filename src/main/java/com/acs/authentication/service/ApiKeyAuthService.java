package com.acs.authentication.service;

import java.util.Map;

import org.springframework.http.HttpMethod;

import reactor.core.publisher.Mono;

public interface ApiKeyAuthService {

	public Mono<String> callAcsViaKeys(HttpMethod method,Map<String,String> queryParams,Object body);
}
