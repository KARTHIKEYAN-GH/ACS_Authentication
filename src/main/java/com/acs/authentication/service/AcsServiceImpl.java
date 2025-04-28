package com.acs.authentication.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.web.dto.SessionDetails;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {

	@Autowired
	private WebClient webClient;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public Mono<JsonNode> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams, Object body) {

		StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");

		SessionDetails sessionDetails = null;
		String userId = null;

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

		userId = queryParams.get("userId");
		if ("logout".equalsIgnoreCase(command) && userId != null) {
			// In case of logout, remove session from Redis
			System.out.println("Logging out user: " + userId); // Log the userId
			// Delete session data from Redis
			redisTemplate.opsForValue().getOperations().delete("session:" + userId);
			System.out.println("Deleted session from Redis for user: " + userId);
		}
		// Execute API call
		return requestSpec.exchangeToMono(response -> handleResponse(response, command));
	}

	private Mono<JsonNode> handleResponse(ClientResponse response, String command) {
		return response.bodyToMono(JsonNode.class).map(body -> {
			String userId = null; // Declare userId here to make it accessible in both blocks
			JsonNode loginResponse = body.get("loginresponse");
			System.out.println("handle reponse executed ");
			if ("login".equalsIgnoreCase(command)) {

				if (loginResponse != null && loginResponse.has("sessionkey")) {
					String sessionKey = loginResponse.get("sessionkey").asText();
					System.out.println("Captured sessionkey after login: " + sessionKey);

					String setCookie = response.headers().asHttpHeaders().getFirst("Set-Cookie");
					String jsessionId = null;
					if (setCookie != null && setCookie.contains("JSESSIONID")) {
						jsessionId = extractJSessionId(setCookie);
						System.out.println("Captured JSESSIONID after login: " + jsessionId);
					}

					// Save to Redis
					SessionDetails sessionDetails = new SessionDetails();
					sessionDetails.setSessionkey(sessionKey);
					sessionDetails.setJsessionid(jsessionId);

					userId = loginResponse.get("userid").asText();
					// Add to cache
					redisTemplate.opsForValue().set("session:" + userId, sessionDetails);
					redisTemplate.expire("session:" + userId, 3600, TimeUnit.SECONDS); // one hour
					System.out.println("Captured sessionkey & JSESSIONID for user: " + userId);
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
