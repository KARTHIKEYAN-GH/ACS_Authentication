package com.acs.authentication.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.authentication.entity.User;
import com.acs.authentication.handler.GenericRequestHandler;
import com.acs.authentication.service.AcsService;
import com.acs.authentication.service.UserService;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateNetworkRequest;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.ListNetworksDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.LoginResponse;
import com.acs.web.dto.SessionDetails;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {

	@Autowired
	private GenericRequestHandler requestHandler;

	public Mono<ResponseEntity<JsonNode>> login(LoginRequest loginRequest) {
		String command = "login";
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("username", loginRequest.getUsername());
		queryParams.put("password", loginRequest.getPassword());

		if (!loginRequest.getUsername().equalsIgnoreCase("admin")) {
			if (loginRequest.getDomain() == null || loginRequest.getDomain().isEmpty()) {
				return Mono.just(requestHandler.error("Enter Domain: e.g. \\\"domain\\\": \\\"sample@gmail.com\\\""));
			}
			queryParams.put("domain", loginRequest.getDomain());
		}

		return requestHandler.handleRequest(HttpMethod.POST, command, queryParams, loginRequest);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> getUserKeys(GetUserKeysDTO getUserKeysDTO,String jwt) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("userId", getUserKeysDTO.getUserId());
		queryParams.put("domainId", getUserKeysDTO.getDomainId());
		queryParams.put("id", getUserKeysDTO.getId());
		return requestHandler.handleRequest(HttpMethod.GET, "getUserKeys", queryParams, jwt);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> listNetworks(ListNetworksDTO listNetworksDTO) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("userId", listNetworksDTO.getUserId());
		queryParams.put("domainId", listNetworksDTO.getDomainId());
		return requestHandler.handleRequest(HttpMethod.GET, "listNetworks", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> logout(String userId) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("userId", userId);
		return requestHandler.handleRequest(HttpMethod.GET, "logout", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> createNetwork(CreateNetworkDTO createNetworkDTO) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("userId", createNetworkDTO.getUserId());
		queryParams.put("domainid", createNetworkDTO.getDomainid());
		queryParams.put("name", createNetworkDTO.getName());
		queryParams.put("zoneId", createNetworkDTO.getZoneId());
		queryParams.put("networkOfferingId", createNetworkDTO.getNetworkOfferingId());
		return requestHandler.handleRequest(HttpMethod.GET, "createNetwork", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> deleteNetwork(DeleteNetworkDTO deleteNetworkDTO) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", deleteNetworkDTO.getId());
		queryParams.put("forced", String.valueOf(deleteNetworkDTO.isForced()));
		queryParams.put("userId", deleteNetworkDTO.getUserId());
		return requestHandler.handleRequest(HttpMethod.GET, "deleteNetwork", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> queryAsyncJobResult(Map<String, String> param) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("jobid", param.get("jobid"));
		queryParams.put("userId", param.get("userId"));
		return requestHandler.handleRequest(HttpMethod.GET, "queryAsyncJobResult", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> createVolume(CreateVolumeDTO request) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("name", request.getName());
		queryParams.put("diskofferingid", request.getDiskofferingid());
		queryParams.put("zoneid", request.getZoneid());
		queryParams.put("userId", request.getUserId());
		return requestHandler.handleRequest(HttpMethod.GET, "createVolume", queryParams, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> deleteVolume(Map<String, String> Params) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", Params.get("id"));
		queryParams.put("userId", Params.get("userId"));
		return requestHandler.handleRequest(HttpMethod.GET, "deleteVolume", queryParams, null);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> destroyVolume(Map<String, String> Params) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", Params.get("id"));
		if(Params.get("expunge")!=null)
		{
		queryParams.put("expunge", Params.get("expunge"));
		}
		if(!queryParams.isEmpty())
		{
			return requestHandler.handleRequest(HttpMethod.GET, "destroyVolume", queryParams, null);
		}
		return Mono.just(requestHandler.error("Missing required parameter: id"));
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> updateNetwork(UpdateNetworkDTO updateNetworkDTO) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("userId", updateNetworkDTO.getUserId());
		queryParams.put("id", updateNetworkDTO.getId());
		
		if (updateNetworkDTO.getName() != null)
			queryParams.put("name", updateNetworkDTO.getName());			

		if (updateNetworkDTO.getSourcenatipaddress() != null)
			queryParams.put("sourcenatipaddress", updateNetworkDTO.getSourcenatipaddress());

		if (updateNetworkDTO.getNetworkofferingid() != null)
			queryParams.put("networkofferingid", updateNetworkDTO.getNetworkofferingid());

		if (updateNetworkDTO.getDns2() != null)
			queryParams.put("dns2", updateNetworkDTO.getDns2());
		

		return requestHandler.handleRequest(HttpMethod.GET, "updateNetwork", queryParams, null);

	}

}
