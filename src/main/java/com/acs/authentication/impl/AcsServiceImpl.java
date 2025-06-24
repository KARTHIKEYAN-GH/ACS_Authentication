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
import java.util.Date;
import com.acs.authentication.entity.User;
import com.acs.authentication.handler.GenericRequestHandler;
import com.acs.authentication.service.AcsService;
import com.acs.authentication.service.UserService;
import com.acs.authentication.util.JwtUtil;
import com.acs.authentication.util.PasswordCryptoUtil;
import com.acs.authentication.util.SessionInfo;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.TokenResponse;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;	

	public Mono<ResponseEntity<JsonNode>> login(LoginRequest loginRequest) {
	    String decryptedPassword = null;
	    try {
	        decryptedPassword = PasswordCryptoUtil.decrypt(loginRequest.getPassword());
	    } catch (Exception e) {
	        e.printStackTrace();
	        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
	    }

	    Map<String, String> queryParams = new HashMap<>();
	    queryParams.put("username", loginRequest.getUsername());
	    queryParams.put("password", decryptedPassword);

	    if (!loginRequest.getUsername().equalsIgnoreCase("admin")) {
	        queryParams.put("domain", loginRequest.getUsername());
	    }

	    return requestHandler.handleRequest(HttpMethod.POST, "login", queryParams, null, null)
	        .flatMap(response -> {
	            JsonNode body = response.getBody();
	            JsonNode loginResponse = body.path("loginresponse");

	            if (loginResponse.has("errorcode")) {
	                int errorCode = loginResponse.path("errorcode").asInt();
	                String errorText = loginResponse.path("errortext").asText();

	                ObjectMapper mapper = new ObjectMapper();
	                ObjectNode errorBody = mapper.createObjectNode();
	                errorBody.put("error", true);
	                errorBody.put("message", errorText);
	                errorBody.put("code", errorCode);

	                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody));
	            }

	            // ✅ ACS login success, return original response
	            return Mono.just(ResponseEntity.ok(body));
	        });
	}

	@Override
	public Mono<ResponseEntity<JsonNode>> getUserKeys(GetUserKeysDTO getUserKeysDTO) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("domainId", getUserKeysDTO.getDomainId());
		queryParams.put("id", getUserKeysDTO.getId());
		return requestHandler.handleRequest(HttpMethod.GET, "getUserKeys", queryParams, null, null);

	}

	@Override
	public Mono<ResponseEntity<JsonNode>> listNetworks(Map<String, String> param) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("account", param.get("account"));
		queryParams.put("id", param.get("id"));
		queryParams.put("page",param.get("page"));
		queryParams.put("pagesize", param.get("pagesize"));
		queryParams.put("listall", param.get("listall"));
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
		ObjectNode responseNode = objectMapper.createObjectNode();
		Map<String, String> queryParams = new HashMap();
		String networkingId = updateNetworkDTO.getId();

		if (networkingId != null) {
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
		responseNode.put("message", "enter network id");
		return Mono.just(ResponseEntity.badRequest().body(responseNode));
	}

	@Override
	public Mono<ResponseEntity<?>> makeRefreshTokenCall(Map<String, String> tokens) {

		String refreshToken = tokens.get("refreshToken");
		String accessToken = tokens.get("accessToken");

		boolean accessTokenisExpired = jwtUtil.isTokenExpired(accessToken);
		boolean refreshTokenisExpired = jwtUtil.isTokenExpired(refreshToken);

		String usernameofresfreshToken = jwtUtil.getSubject(refreshToken);
		String usernameofaccessToken = jwtUtil.getSubject(accessToken);

		ObjectNode responseNode = objectMapper.createObjectNode();

		if (usernameofresfreshToken.equals(usernameofaccessToken)) {

			if (accessTokenisExpired && !refreshTokenisExpired) {
				String sessionKey =jwtUtil.getSessionKey(accessToken);
				 String newAccessToken=jwtUtil.generateToken(usernameofresfreshToken, sessionKey);
				 String newRefreshToken=jwtUtil.generateRefreshToken(usernameofaccessToken);
				 TokenResponse response = new TokenResponse();
				 response.setAccessToken(newAccessToken);
				 response.setRefreshToken(newRefreshToken); 
				 return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(response));
			}
			responseNode.put("message", "Token Expired Please login");
	        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseNode));
			}
		responseNode.put("message", "InvalidTokens");
		return Mono.just(ResponseEntity.badRequest().body(responseNode));
	}

		@Override
		public ResponseEntity<?> keepAlive(TokenResponse tokens) {
		    System.out.println("Keep-alive call triggered");

		    String accessToken = tokens.getAccessToken().replace("Bearer ", "");
		    String refreshToken = tokens.getRefreshToken();

		    String sessionKey = jwtUtil.getSessionKey(accessToken);
		    String redisKey = "session:" + sessionKey;

		    if (!redisTemplate.hasKey(redisKey)) {
		        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
		    }

		    // 1. Check Redis TTL
		    Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.MINUTES);
		    if (ttl != null && ttl <= 2) {
		        redisTemplate.expire(redisKey, 4, TimeUnit.MINUTES);
		        System.out.println("TTL extended to 4 mins");
		    }

		    // 2. Check refreshToken expiration
		    Date refreshExp = jwtUtil.extractExpiration(refreshToken); // use refreshToken here
		    long secondsLeft = (refreshExp.getTime() - System.currentTimeMillis()) / 1000;

		    if (secondsLeft <= 200) { // less than 3.5 minutes
		        String username = jwtUtil.getSubject(accessToken);

		        String newAccess = jwtUtil.generateToken(username, sessionKey);
		        String newRefresh = jwtUtil.generateRefreshToken(username);

		        // Extend Redis TTL
		        redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);

		        Map<String, String> newTokens = Map.of(
		            "accessToken", newAccess,
		            "refreshToken", newRefresh
		        );

		        return ResponseEntity.ok(Map.of("newTokens", newTokens));
		    }

		    return ResponseEntity.ok(Map.of("message", "Keepalive successful"));
	}

//	@Override
//	public ResponseEntity<?> keepAlive(Map<String, String> tokens) {
//		System.out.println("keep alive call triggered ");
//		//String accessToken = tokens.replace("Bearer ", "");
//		String AccessToken=tokens.get("accessToken");
//		String accessToken =AccessToken.replace("Bearer ", "");
//	    String sessionKey = jwtUtil.getSessionKey(accessToken);
//	    String redisKey = "session:" + sessionKey;
//
//	    if (!redisTemplate.hasKey(redisKey)) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
//	    }
//
//	    // 1. Check TTL
//	    Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.MINUTES);
//	    if (ttl != null && ttl <= 2) {
//	        redisTemplate.expire(redisKey, 4, TimeUnit.MINUTES);
//	        System.out.println("TTL extended to 4 mins");
//	    }
//
//	    // 2. Check refresh token expiry
//	    Date refreshExp = jwtUtil.extractExpiration(tokens.getBytes());
//	    long secondsLeft = (refreshExp.getTime() - System.currentTimeMillis()) / 1000;
//	    if (secondsLeft <= 200) {  // <= 5 mins
//	        // Call refresh logic
//	    	String username=jwtUtil.getSubject(accessToken);
//	        String newAccess = jwtUtil.generateToken(username, sessionKey); // Include claims as needed
//	        String newRefresh = jwtUtil.generateRefreshToken(username);
//	        
//	        System.out.println("newAccess : "+newAccess);
//	        System.out.println("newRefresh :"+ newRefresh);
//	        // Optionally update Redis too
//	        redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);
//
//	        Map<String, String> newTokens = Map.of(
//	            "accessToken", newAccess,
//	            "refreshToken", newRefresh
//	        );
//
//	        return ResponseEntity.ok(Map.of("newTokens", newTokens));
//	    }
//
//	    return ResponseEntity.ok(Map.of("message", "Keepalive successful"));
//	}		
}

