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

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cloudstack")
public class LoginController {

	
	@Autowired
	private AcsService acsService;

	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Test successful!");
	}

	@PostMapping("/{command}") //command=login
	public Mono<ResponseEntity<String>> callAcsApi(@PathVariable String command,
			@RequestBody LoginRequest loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
		String domain = loginRequest.getDomain();

		if (username == null || username.isEmpty()) {
			return Mono.just(ResponseEntity.badRequest().body("Username is required."));
		}

		if (!username.equalsIgnoreCase("admin") && (domain == null || domain.isEmpty())) {
			return Mono.just(ResponseEntity.badRequest().body("domain : is required."));
		}

		Map<String, String> queryParams = new HashMap<>();

		queryParams.put("username", username);
		queryParams.put("password", password);
		if (!username.equalsIgnoreCase("admin")) {
			queryParams.put("domain", domain);
		}
		// Pass loginRequest as the body, since it's the data to be sent in the POST
		// request
		Mono<String> responseMono = acsService.callAcsApi(HttpMethod.POST, command, queryParams, loginRequest);

		// Return the response reactively
		return responseMono.map(response -> ResponseEntity.ok(response)).onErrorResume(e -> Mono
				.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage())));
	}
	
	@GetMapping("/{command}") //command=listNetworks
	public Mono<ResponseEntity<String>> callAcsApi(@PathVariable String command,@RequestParam String domainId,@RequestParam String sessionkey) {
		if(domainId !=null && sessionkey != null) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put("domainid", domainId);
			queryParams.put("sessionkey", sessionkey);
			Mono<String> responseMono = acsService.callAcsApi(HttpMethod.GET, command, queryParams,null);
			return responseMono.map(response -> ResponseEntity.ok(response)).onErrorResume(e -> Mono
					.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage())));
		}
		else 
		{
			return Mono.just(ResponseEntity.badRequest().body("domainId and sessionkey is required."));
		}
	}

}
