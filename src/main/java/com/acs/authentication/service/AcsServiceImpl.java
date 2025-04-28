package com.acs.authentication.service;

import java.lang.module.FindException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.authentication.entity.User;
import com.acs.authentication.util.SignatureUtil;
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
		if ("logout".equalsIgnoreCase(command) && uuid != null) {
			// In case of logout, remove session from Redis
			System.out.println("Logging out user: " + uuid); // Log the userId
			// Delete session data from Redis
			redisTemplate.opsForValue().getOperations().delete("session:" + uuid);
			System.out.println("Deleted session from Redis for user: " + uuid);
		}
		// Execute API call
		return requestSpec.exchangeToMono(response -> handleResponse(response, command, uuid));
	}

	private Mono<JsonNode> handleResponse(ClientResponse response, String command, String userId) {
		return response.bodyToMono(JsonNode.class).map(body -> {
			// final String userId; // Declare userId here to make it accessible in both
			// blocks
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

					String usersId = loginResponse.get("userid").asText();
					// Add to cache

					redisTemplate.opsForValue().set("session:" + usersId, sessionDetails);
					redisTemplate.expire("session:" + usersId, 3600, TimeUnit.SECONDS); // one hour
					System.out.println("Captured sessionkey & JSESSIONID for user: " + usersId);
				}
			} else if ("getUserKeys".equalsIgnoreCase(command)) {
				JsonNode getuserkeysresponse = body.get("getuserkeysresponse");

				if (getuserkeysresponse != null) {
					JsonNode userkeys = getuserkeysresponse.get("userkeys");
					if (userkeys != null) {
						String apikey = userkeys.get("apikey").asText();
						String secretkey = userkeys.get("secretkey").asText();

						// Check if user already exists in the database
						User existingUser = userService.findByUserId(userId);
						if (existingUser != null) {
							// Update user keys if needed
							existingUser.setApiKey(apikey);
							existingUser.setSecretKey(secretkey);
							userService.save(existingUser);
						} else {
							// Create a new user if user doesn't exist
							User keys = new User();
							keys.setUserId(userId);
							keys.setApiKey(apikey);
							keys.setSecretKey(secretkey);
							userService.save(keys);
						}

						System.out.println("Saved user API keys and secret key to the database.");
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

//	@Override
//	public Mono<JsonNode> listNetworksByKeys(HttpMethod post, String command, Map<String, String> queryParams,
//			Object object) {
//		User user = userService.findByUserId(queryParams.get("userId"));
//		if(user !=null)
//		{		
//			SignatureUtil sg=new SignatureUtil();
//		String finalUrl = sg.generateSignedUrl(queryParams, user.getSecretKey());
//		return (Mono<JsonNode>) WebClient.create();
//		JsonNode response = webClient.get()
//			    .uri(finalUrl)
//			    .retrieve()
//			    .bodyToMono(JsonNode.class)
//			    .block();}
//		else {
//		// need to throw exception 	
//		}
//		}
//	
//	@Override
//	public Mono<JsonNode> listNetworksByKeys(String command, Map<String, String> queryParams, Object object) {
//	    // Retrieve the user by userId from the queryParams
//	    User user = userService.findByUserId(queryParams.get("userId"));
//	    
//	    if (user != null) {
//	        // Create SignatureUtil instance and generate the signed URL
//	        SignatureUtil sg = new SignatureUtil();
//	        String finalUrl = sg.generateSignature(queryParams, user.getSecretKey());
//
//	        // Make the actual API call using WebClient
//	        return WebClient.create()
//	                .get()  // Use GET method (or POST if required by API)
//	                .uri(finalUrl)  // Pass the signed URL as the URI
//	                .retrieve()  // Retrieve the response
//	                .bodyToMono(JsonNode.class)  // Convert the response to JsonNode
//	                .onErrorMap(throwable -> new FindException("API request failed", throwable));  // Add error handling
//	    } else {
//	        // Throw an exception if the user is not found
//	        return Mono.error(new IllegalArgumentException("User not found"));
//	    }
//	}
//
//}
