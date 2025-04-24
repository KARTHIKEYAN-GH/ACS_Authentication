package com.acs.authentication.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs.authentication.service.AcsService;
import com.acs.web.dto.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cloudstack")
public class LoginController {

	
	@Autowired
	private AcsService acsService;

	@Autowired
    private ObjectMapper objectMapper; // make sure this bean is available

	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Test successful!");
	}

	
    @PostMapping("/{command}") // e.g., /api/cloudstack/login
    public Mono<ResponseEntity<JsonNode>> callAcsApi(@PathVariable String command,
                                                     @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String domain = loginRequest.getDomain();

        // Validation
        if (username == null || username.isEmpty()) {
            return Mono.just(error("Username is required."));
        }

        if (!username.equalsIgnoreCase("admin") && (domain == null || domain.isEmpty())) {
            return Mono.just(error("Domain is required for non-admin users."));
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("username", username);
        queryParams.put("password", password);
        if (!username.equalsIgnoreCase("admin")) {
            queryParams.put("domain", domain);
        }

        // Make the call
        return acsService.callAcsApi(HttpMethod.POST, command, queryParams, loginRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    e.printStackTrace(); // optional for debugging
                    return Mono.just(serverError("Internal Server Error: " + e.getMessage()));
                });
    }

    // Helper methods for error response
    private ResponseEntity<JsonNode> error(String message) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", message);
        return ResponseEntity.badRequest().body(errorJson);
    }

    private ResponseEntity<JsonNode> serverError(String message) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJson);
    }

	@GetMapping("/{command}") // e.g., /acs/listNetworks
	public Mono<ResponseEntity<JsonNode>> callAcsApi(@PathVariable String command,
	                                                 @RequestParam String domainId,
	                                                 @RequestParam String sessionkey) {
	    if (domainId != null && sessionkey != null) {
	        Map<String, String> queryParams = new HashMap<>();
	        queryParams.put("domainId", domainId);
	        queryParams.put("sessionkey", sessionkey);

	        Mono<JsonNode> responseMono = acsService.callAcsApi(HttpMethod.GET, command, queryParams, null);

	        return responseMono
	                .map(ResponseEntity::ok)
	                .onErrorResume(e -> Mono.just(
	                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                                      .body(null)));
	    } else {
	        return Mono.just(ResponseEntity.badRequest().body(null));
	    }
	}


}
