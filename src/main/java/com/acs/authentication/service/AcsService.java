package com.acs.authentication.service;

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateNetworkRequest;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.LoginResponse;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

public interface AcsService {

	Mono<JsonNode> callAcsApi(HttpMethod post, String command, Map<String, String> queryParams, Object object);
}
