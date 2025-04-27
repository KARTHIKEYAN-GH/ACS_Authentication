package com.acs.authentication.service;

import java.util.Map;

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

	private String sessionKey;
	private String jsessionId;

	@Override
	public Mono<JsonNode> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams, Object body) {

		StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");

		// Only check Redis for sessionKey if the command is not "login"
		if (!"login".equalsIgnoreCase(command) && (queryParams.containsKey("userId"))) {
			String userId = queryParams.get("userId");
			System.out.println("userID is " + userId);
			String sessionKey = getSessionKeyFromRedis(userId); // Fetch from Redis if exists
			System.out.println("sessionKey is " + sessionKey);
			if (sessionKey != null) {
				queryParams.put("sessionkey", sessionKey); // Set sessionkey from Redis
			}
		}

		queryParams.forEach((key, value) -> {
			if (!"userId".equalsIgnoreCase(key)) {
				urlBuilder.append("&").append(key).append("=").append(value);
			}
		});
		String finalUri = urlBuilder.toString();

		WebClient.RequestHeadersSpec<?> request;
		if (method == HttpMethod.POST || method == HttpMethod.PUT) {
			request = (body != null) ? webClient.method(method).uri(finalUri).bodyValue(body)
					: webClient.method(method).uri(finalUri);
		} else {
			request = webClient.method(method).uri(finalUri);
		}
		WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(finalUri);

		if (sessionKey != null) {
			requestSpec = requestSpec.cookie("sessionkey", sessionKey);
		}
		if (jsessionId != null) {
			requestSpec = requestSpec.cookie("JSESSIONID", jsessionId);
		}

		// ⭐⭐ Print header cookie info before making the API call ⭐⭐
		if (!"login".equalsIgnoreCase(command)) {
			System.out.println("----- Sending Cookies in Request Header -----");
			System.out.println("sessionkey = " + sessionKey);
			System.out.println("JSESSIONID = " + jsessionId);
			System.out.println("---------------------------------------------");
		}

		if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
			requestSpec = (RequestBodySpec) requestSpec.bodyValue(body);
		}

		return requestSpec.exchangeToMono(response -> handleResponse(response, command));
	}

	private Mono<JsonNode> handleResponse(ClientResponse response, String command) {
		return response.bodyToMono(JsonNode.class).map(body -> {
			if ("login".equalsIgnoreCase(command)) {
				JsonNode loginResponse = body.get("loginresponse");
				if (loginResponse != null && loginResponse.has("sessionkey")) {
					this.sessionKey = loginResponse.get("sessionkey").asText();
					System.out.println(" Captured sessionkey after login: " + this.sessionKey);
				}

				String setCookie = response.headers().asHttpHeaders().getFirst("Set-Cookie");
				if (setCookie != null && setCookie.contains("JSESSIONID")) {
					jsessionId = extractJSessionId(setCookie);
					System.out.println(" Captured JSESSIONID after login: " + jsessionId);
				}

				// Save to Redis
				SessionDetails sessionDetails = new SessionDetails();
				sessionDetails.setSessionkey(this.sessionKey); // use 'this.sessionKey'
				sessionDetails.setJsessionid(jsessionId);

				String userId = loginResponse.get("userid").asText();
				redisTemplate.opsForValue().set("session:" + userId, sessionDetails);

				System.out.println(" Captured sessionkey & JSESSIONID for user: " + userId);
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

	// fetch sessionkey from Redis
	private String getSessionKeyFromRedis(String userId) {
		// String userId = getLoggedInUserId(); // Get the userId dynamically (could be
		// from security context, etc.)
		// System.out.println("user id is :"+userId);
		SessionDetails sessionDetails = (SessionDetails) redisTemplate.opsForValue().get("session:" + userId);
		return sessionDetails != null ? sessionDetails.getSessionkey() : null;
	}

//      // Dummy placeholder method to get logged-in userId
//         private String getLoggedInUserId() {
//          return "---someUserId---";  // Replace with real implementation, e.g., from JWT token, or security context.
//      }

}
