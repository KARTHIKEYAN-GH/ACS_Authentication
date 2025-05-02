package com.acs.authentication.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.ListNetworksDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

public interface AcsService {
	
	Mono<ResponseEntity<JsonNode>> login(LoginRequest loginRequest);
	Mono<ResponseEntity<JsonNode>> logout(String userId );
	//Mono<ResponseEntity<JsonNode>> getUserKeys(GetUserKeysDTO getUserKeysDTO);
	Mono<ResponseEntity<JsonNode>> listNetworks(ListNetworksDTO listNetworksDTO);
	Mono<ResponseEntity<JsonNode>> createNetwork(CreateNetworkDTO createNetworkDTO);
	Mono<ResponseEntity<JsonNode>> deleteNetwork(DeleteNetworkDTO deleteNetworkDTO);
	Mono<ResponseEntity<JsonNode>> queryAsyncJobResult(Map<String, String> param);
	Mono<ResponseEntity<JsonNode>> createVolume(CreateVolumeDTO request);
	Mono<ResponseEntity<JsonNode>> deleteVolume(Map<String, String> queryParams);
	Mono<ResponseEntity<JsonNode>> destroyVolume(Map<String, String> queryParams);
	Mono<ResponseEntity<JsonNode>> updateNetwork(UpdateNetworkDTO updateNetworkDTO);
	Mono<ResponseEntity<JsonNode>> getUserKeys(GetUserKeysDTO getUserKeysDTO, String jwt);
	
	
}
