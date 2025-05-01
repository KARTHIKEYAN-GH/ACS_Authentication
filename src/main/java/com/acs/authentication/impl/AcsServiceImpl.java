package com.acs.authentication.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.authentication.entity.User;
import com.acs.authentication.service.AcsService;
import com.acs.authentication.service.UserService;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateNetworkRequest;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.LoginResponse;
import com.acs.web.dto.SessionDetails;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {
	
	@Autowired
	private WebClient webClient;

	@Autowired
	private UserService userService;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public Mono<JsonNode> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams, Object body) {

		StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");

		SessionDetails sessionDetails = null;
		final String userId;

		// Fetch session details from Redis if command is NOT login
		if (!"login".equalsIgnoreCase(command) && queryParams.containsKey("userId")) {
			userId = queryParams.get("userId");
			System.out.println("userID is " + userId);
			sessionDetails = getSessionDetailsFromRedis(userId);

			if (sessionDetails != null && sessionDetails.getSessionkey() != null) {
				queryParams.put("sessionkey", sessionDetails.getSessionkey());
			}
		}
		// Build final URL
		queryParams.forEach((key, value) -> {
			if (!"userId".equalsIgnoreCase(key)) { // skip userId
				urlBuilder.append("&").append(key).append("=").append(value);
			}
		});
		String finalUri = urlBuilder.toString();
		// Prepare WebClient Request
		WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(finalUri);

		// Add cookies dynamically if available
		if (sessionDetails != null) {
			if (sessionDetails.getSessionkey() != null) {
				requestSpec = requestSpec.cookie("sessionkey", sessionDetails.getSessionkey());
			}
			if (sessionDetails.getJsessionid() != null) {
				requestSpec = requestSpec.cookie("JSESSIONID", sessionDetails.getJsessionid());
			}
		}
		if (!"login".equalsIgnoreCase(command)) {
			System.out.println("----- Sending Cookies in Request Header -----");
			System.out.println("sessionkey = " + (sessionDetails != null ? sessionDetails.getSessionkey() : "null"));
			System.out.println("JSESSIONID = " + (sessionDetails != null ? sessionDetails.getJsessionid() : "null"));
			System.out.println("---------------------------------------------");
		}
		// Add body if required
		if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
			requestSpec = (RequestBodySpec) requestSpec.bodyValue(body);
		}
		String uuid = queryParams.get("userId"); // userId

		// In case of logout, remove session from Redis
		if ("logout".equalsIgnoreCase(command) && uuid != null) {
			System.out.println("Logging out user: " + uuid); // Log the userId
			redisTemplate.opsForValue().getOperations().delete("session:" + uuid); // Delete session data from Redis
		}

		return requestSpec.exchangeToMono(response -> handleResponse(response, command, uuid,queryParams)); // Execute API call
	}

	private Mono<JsonNode> handleResponse(ClientResponse response, String command, String userId,Map<String, String> queryParams) {
		return response.bodyToMono(JsonNode.class).map(body -> {
			JsonNode loginResponse = body.get("loginresponse");
			if ("login".equalsIgnoreCase(command)) {

				if (loginResponse != null && loginResponse.has("sessionkey")) {
					String sessionKey = loginResponse.get("sessionkey").asText();
					System.out.println("Captured sessionkey after login: " + sessionKey);
					
					//extracts the Set-Cookie header from the HTTP response.
					String setCookie = response.headers().asHttpHeaders().getFirst("Set-Cookie");
					String jsessionId = null;
					if (setCookie != null && setCookie.contains("JSESSIONID")) {
						jsessionId = extractJSessionId(setCookie);
						System.out.println("Captured JSESSIONID after login: " + jsessionId);
					}

					SessionDetails sessionDetails = new SessionDetails(); // Save to Redis
					sessionDetails.setSessionkey(sessionKey);
					sessionDetails.setJsessionid(jsessionId);

					String usersId = loginResponse.get("userid").asText();

					redisTemplate.opsForValue().set("session:" + usersId, sessionDetails); // added to cache
					redisTemplate.expire("session:" + usersId, 3600, TimeUnit.SECONDS); // one hour
					System.out.println("Captured sessionkey & JSESSIONID for user: " + usersId);
					
					User existingUser = userService.findByUserId(usersId);
					if(existingUser == null)
					{
						
						User user = new User();
						user.setUserName(loginResponse.get("username").asText());
						user.setUserId(loginResponse.get("userid").asText());
						user.setEmail(loginResponse.get("username").asText());
						user.setIsActive(true);
						userService.save(user);
					}
					
				}
			} else if ("getUserKeys".equalsIgnoreCase(command)) {
				JsonNode getuserkeysresponse = body.get("getuserkeysresponse");

				if (getuserkeysresponse != null) {
					JsonNode userkeys = getuserkeysresponse.get("userkeys");
					if (userkeys != null) {
						String apikey = userkeys.get("apikey").asText();
						String secretkey = userkeys.get("secretkey").asText();
						User existingUser = userService.findByUserId(userId);
						if((existingUser.getApiKey() ==null) && (existingUser.getSecretKey() ==null)) {
							existingUser.setApiKey(apikey);
							existingUser.setSecretKey(secretkey);
							userService.save(existingUser);
						} 
					}
				}
			}
			return body;
		});
	}

	private String extractJSessionId(String setCookie) {
		for (String cookie : setCookie.split(";")) {
			if (cookie.trim().startsWith("JSESSIONID")) {
				return cookie.split("=")[1];
			}
		}
		return null;
	}

	// Fetch both sessionkey and jsessionid from Redis
	private SessionDetails getSessionDetailsFromRedis(String userId) {
		return (SessionDetails) redisTemplate.opsForValue().get("session:" + userId);
	}

	
}
