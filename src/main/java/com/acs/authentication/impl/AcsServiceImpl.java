package com.acs.authentication.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.acs.authentication.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.acs.authentication.entity.User;
import com.acs.authentication.handler.GenericRequestHandler;
import com.acs.authentication.service.AcsService;
import com.acs.authentication.service.UserService;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {

	@Autowired
	private PasswordCryptoUtil PasswordCryptoUtil;

	@Autowired
	private GenericRequestHandler requestHandler;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	public Mono<ResponseEntity<JsonNode>> login(LoginRequest loginRequest) {
		String decryptedPassword = null;
		try {
			decryptedPassword = PasswordCryptoUtil.decrypt(loginRequest.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("username", loginRequest.getUsername());
		queryParams.put("password", decryptedPassword);

		if (!loginRequest.getUsername().equalsIgnoreCase("admin")) {
			if (loginRequest.getDomain() == null || loginRequest.getDomain().isEmpty()) {
				return Mono.just(requestHandler.error("Enter Domain: example@gmail.com"));
			}
			queryParams.put("domain", loginRequest.getDomain());
		}

		return requestHandler.handleRequest(HttpMethod.POST, "login", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> getUserKeys(GetUserKeysDTO getUserKeysDTO) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("userId", getUserKeysDTO.getUserId());
		queryParams.put("domainId", getUserKeysDTO.getDomainId());
		queryParams.put("id", getUserKeysDTO.getId());
		return requestHandler.handleRequest(HttpMethod.GET, "getUserKeys", queryParams, null, null);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> listNetworks(Map<String, String> param) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("domainId", param.get("domainId"));
		return requestHandler.handleRequest(HttpMethod.GET, "listNetworks", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> logout() {
		Map<String, String> queryParams = new HashMap<>();
		return requestHandler.handleRequest(HttpMethod.GET, "logout", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> createNetwork(CreateNetworkDTO createNetworkDTO) {
		Map<String, String> queryParams = new HashMap();
		// queryParams.put("userId", createNetworkDTO.getUserId());
		queryParams.put("domainid", createNetworkDTO.getDomainid());
		queryParams.put("name", createNetworkDTO.getName());
		queryParams.put("zoneId", createNetworkDTO.getZoneId());
		queryParams.put("networkOfferingId", createNetworkDTO.getNetworkOfferingId());
		return requestHandler.handleRequest(HttpMethod.GET, "createNetwork", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> deleteNetwork(DeleteNetworkDTO deleteNetworkDTO) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", deleteNetworkDTO.getId());
		queryParams.put("forced", String.valueOf(deleteNetworkDTO.isForced()));
		return requestHandler.handleRequest(HttpMethod.GET, "deleteNetwork", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> queryAsyncJobResult(Map<String, String> param) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("jobid", param.get("jobid"));
		queryParams.put("userId", param.get("userId"));
		return requestHandler.handleRequest(HttpMethod.GET, "queryAsyncJobResult", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> createVolume(CreateVolumeDTO request) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("name", request.getName());
		queryParams.put("diskofferingid", request.getDiskofferingid());
		queryParams.put("zoneid", request.getZoneid());
		queryParams.put("userId", request.getUserId());
		return requestHandler.handleRequest(HttpMethod.GET, "createVolume", queryParams, null, null);
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> deleteVolume(Map<String, String> Params) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", Params.get("id"));
		queryParams.put("userId", Params.get("userId"));
		return requestHandler.handleRequest(HttpMethod.GET, "deleteVolume", queryParams, null, null);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> destroyVolume(Map<String, String> Params) {
		Map<String, String> queryParams = new HashMap();
		queryParams.put("id", Params.get("id"));
		if (Params.get("expunge") != null) {
			queryParams.put("expunge", Params.get("expunge"));
		}
		if (!queryParams.isEmpty()) {
			return requestHandler.handleRequest(HttpMethod.GET, "destroyVolume", queryParams, null, null);
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

		return requestHandler.handleRequest(HttpMethod.GET, "updateNetwork", queryParams, null, null);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> makeRefreshTokenCall(Map<String, String> tokens) {

		String refreshToken = tokens.get("refreshToken");
		String accessToken = tokens.get("accessToken");

		boolean accessTokenisExpired = jwtUtil.isTokenExpired(refreshToken);
		boolean refreshTokenisExpired = jwtUtil.isTokenExpired(accessToken);

		String usernameofresfreshToken = jwtUtil.getSubject(refreshToken);
		String usernameofaccessToken = jwtUtil.getSubject(accessToken);

		Map<String, String> queryParams = new HashMap<>();
		ObjectNode responseNode = objectMapper.createObjectNode();

		if (usernameofresfreshToken.equals(usernameofaccessToken)) {

			if (accessTokenisExpired && !refreshTokenisExpired) {

				User user = userService.findByUserName(usernameofresfreshToken);
				queryParams.put("username", user.getUserName());
				queryParams.put("password", user.getPassword());

				if (!user.getUserName().equalsIgnoreCase("admin")) {
					queryParams.put("domain", user.getUserName());
				}
				return requestHandler.handleRequest(HttpMethod.POST, "login", queryParams, null, null);
			}
			responseNode.put("message", "Tokens Expired");
			return Mono.just(ResponseEntity.badRequest().body(responseNode));
		}
		responseNode.put("message", "InvalidTokens");
		return Mono.just(ResponseEntity.badRequest().body(responseNode));
	}

}
