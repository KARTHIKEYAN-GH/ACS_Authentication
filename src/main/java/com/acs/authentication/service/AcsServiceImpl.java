package com.acs.authentication.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.acs.authentication.entity.User;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {

    @Autowired
    private WebClient webClient;

    @Override
    public Mono<JsonNode> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams,
                                      @Nullable Object body) {
    	
    	String baseurl="http://10.30.11.31:8080/client/api";
    	
        StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");
        queryParams.forEach((key, value) -> urlBuilder.append("&").append(key).append("=").append(value));
        String finalUri = urlBuilder.toString();
        
        //webClient.method(method).uri(baseurl + finalUri)
        
        WebClient.RequestHeadersSpec<?> request;
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            request = (body != null) ? webClient.method(method).uri(finalUri).bodyValue(body)
                                     : webClient.method(method).uri(finalUri);
        } else {
            request = webClient.method(method).uri(finalUri);
        }

        return request.exchangeToMono(response ->
            response.bodyToMono(JsonNode.class) // Success or error JSON from ACS
        );
    }
}


