package com.acs.authentication.impl;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.authentication.exception.ACSException;
import com.acs.authentication.service.ApiKeyAuthService;
import com.acs.authentication.util.SignatureUtil;

import reactor.core.publisher.Mono;

@Service
public class ApiKeyAuthServiceImpl implements ApiKeyAuthService {

	@Autowired
	private WebClient webClient;

	@Autowired
	private SignatureUtil signatureUtil;

	@Override
	public Mono<String> callAcsViaKeys(HttpMethod method, Map<String, String> queryParams, Object body) {

		String finalUrl = signatureUtil.generateSignature(queryParams);

		System.out.println("Final Url is :" + finalUrl);
		WebClient.RequestHeadersSpec<?> requestSpec = webClient.method(method).uri(URI.create(finalUrl));
		if (method == HttpMethod.POST || method == HttpMethod.PUT) {
			requestSpec = ((RequestBodySpec) requestSpec).bodyValue(body);
		}

		return requestSpec.retrieve()
				.onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class).flatMap(errorBody -> {
					// log.error("Error response from ACS: {}", errorBody);
					return Mono.error(new ACSException("ACS Error: " + errorBody));
				})).bodyToMono(String.class);
	}
}
