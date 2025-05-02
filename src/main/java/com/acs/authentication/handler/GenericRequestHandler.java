package com.acs.authentication.handler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.acs.authentication.entity.User;
import com.acs.authentication.service.ApiKeyAuthService;
import com.acs.authentication.service.UserService;
import com.acs.authentication.util.JwtUtil;
import com.acs.web.dto.SessionDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Component
public class GenericRequestHandler {

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private WebClient webClient;

	@Autowired
	private UserService userService;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ApiKeyAuthService apiKeyAuthService;

	public <T> Mono<ResponseEntity<String>> handle(T requestDto, HttpMethod method) {
		Map<String, String> queryParams = objectMapper.convertValue(requestDto, new TypeReference<>() {
		});

		return apiKeyAuthService.callAcsViaKeys(method, queryParams, null).map(ResponseEntity::ok).onErrorResume(e -> {
			ObjectNode error = JsonNodeFactory.instance.objectNode();
			error.put("error", "ACS Request Failed");
			error.put("message", e.getMessage());
			return Mono.just(ResponseEntity.status(500).body(error.toString()));
		});
	}

	public Mono<ResponseEntity<JsonNode>> handleRequest(HttpMethod method, String command,
			Map<String, String> queryParams, Object body) {
		return callAcsApi(method, command, queryParams, body).map(ResponseEntity::ok).onErrorResume(e -> {
			e.printStackTrace();
			return Mono.just(serverError("Internal Server Error: " + e.getMessage()));
		});
	}

	public Mono<JsonNode> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams, Object body) {

		StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");

		SessionDetails sessionDetails = null;
		final String userId;

		// Fetch session details from Redis if command is NOT login
		if (!"login".equalsIgnoreCase(command) && queryParams.containsKey("userId")) {
			userId = queryParams.get("userId");
			System.out.println("userID is " + userId);
			String session=jwtUtil.extractSessionKey(body.toString());
			System.out.println("Sessionkey is :"+session);
			sessionDetails = getSessionDetailsFromRedis(session);

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
		System.out.println("final Url is :" + finalUri);
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

		return requestSpec.exchangeToMono(response -> handleResponse(response, command, uuid, queryParams)); // Execute
																												// API
																												// call
	}

	private Mono<JsonNode> handleResponse(ClientResponse response, String command, String userId,
			Map<String, String> queryParams) {
		return response.bodyToMono(JsonNode.class).map(body -> {
			JsonNode loginResponse = body.get("loginresponse");
			if ("login".equalsIgnoreCase(command)) {

				if (loginResponse != null && loginResponse.has("sessionkey")) {
					String sessionKey = loginResponse.get("sessionkey").asText();
					System.out.println("Captured sessionkey after login: " + sessionKey);

					// extracts the Set-Cookie header from the HTTP response.
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

					redisTemplate.opsForValue().set("session:" + sessionKey, sessionDetails); // added to cache
					redisTemplate.expire("session:" + usersId, 3600, TimeUnit.SECONDS); // one hour

					String jwtToken = jwtUtil.generateToken(loginResponse.get("username").asText(), userId, sessionKey);
					 ObjectNode responseWithToken = (ObjectNode) loginResponse;
					   responseWithToken.put("token", jwtToken);
					System.out.println("Captured sessionkey & JSESSIONID for user: " + usersId);

					User existingUser = userService.findByUserId(usersId);
					if (existingUser == null) {
						User user = new User();
						user.setUserName(loginResponse.get("username").asText());
						user.setUserId(loginResponse.get("userid").asText());
						user.setEmail(loginResponse.get("username").asText());
						user.setDomainUuid(loginResponse.get("domainid").asText());
						user.setIsActive(true);
						// the account type (admin, domain-admin, read-only-admin, user)
						String userType = loginResponse.get("type").asText();

						if (userType.equalsIgnoreCase("1")) {
							user.setUserType(user.getUserType().ROOT_ADMIN);
						} else if (userType.equalsIgnoreCase("2")) {
							user.setUserType(user.getUserType().DOMAIN_ADMIN);
						} else if (userType.equalsIgnoreCase("3")) {
							user.setUserType(user.getUserType().READ_ONLY_ADMIN);
						} else {
							user.setUserType(user.getUserType().USER);
						}
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
						if ((existingUser.getApiKey() == null) && (existingUser.getSecretKey() == null)) {
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

	public ResponseEntity<JsonNode> error(String message) {
		ObjectNode errorJson = objectMapper.createObjectNode();
		errorJson.put("error", message);
		return ResponseEntity.badRequest().body(errorJson);
	}

	private ResponseEntity<JsonNode> serverError(String message) {
		ObjectNode errorJson = objectMapper.createObjectNode();
		errorJson.put("error", message);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJson);
	}
}
