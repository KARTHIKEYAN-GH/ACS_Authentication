package com.acs.authentication.controller;

import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs.authentication.service.AcsService;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.TokenResponse;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import reactor.core.publisher.Mono;
@RestController
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(allowedHeaders = "Authorization")
@RequestMapping("/api/cloudstack")
public class ACSController {

	@Autowired
	private AcsService acsService;
	
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Test successful!");
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<JsonNode>> login(@RequestBody LoginRequest loginRequest) {
		return acsService.login(loginRequest);
	}

	@PostMapping("/refresh")
	public Mono<ResponseEntity<?>> makeRefreshtokenCall(@RequestBody Map<String, String> tokens) {
		return acsService.makeRefreshTokenCall(tokens);

	}

	@GetMapping("/logout")
	public Mono<ResponseEntity<JsonNode>> logout() {
		return acsService.logout();
	}
	
	@PostMapping("/getUserKeys")
	public Mono<ResponseEntity<JsonNode>> getUserKeys(@RequestBody GetUserKeysDTO getUserKeysDTO) {
		return acsService.getUserKeys(getUserKeysDTO);
	}

	@GetMapping("/listNetworks")
	public Mono<ResponseEntity<JsonNode>> listNetworks(@RequestParam Map<String, String> queryParams) {
		return acsService.listNetworks(queryParams);
	}

	@GetMapping("/createNetwork")
	public Mono<ResponseEntity<JsonNode>> createNetwork(@RequestBody CreateNetworkDTO createNetworkDTO) {
		return acsService.createNetwork(createNetworkDTO);
	}

	@GetMapping("/updateNetwork")
	public Mono<ResponseEntity<JsonNode>> updateNetwork(@RequestBody UpdateNetworkDTO updateNetworkDTO) {
		return acsService.updateNetwork(updateNetworkDTO);
	}

	@GetMapping("/deleteNetwork")
	public Mono<ResponseEntity<JsonNode>> deleteNetwork(@RequestBody DeleteNetworkDTO deleteNetworkDTO) {
		return acsService.deleteNetwork(deleteNetworkDTO);
	}

	@GetMapping("/createVolume")
	public Mono<ResponseEntity<JsonNode>> createVolume(@RequestBody CreateVolumeDTO request) {
		return acsService.createVolume(request);
	}

	@GetMapping("/deleteVolume")
	public Mono<ResponseEntity<JsonNode>> deleteVolume(@RequestParam Map<String, String> queryParams) {
		return acsService.deleteVolume(queryParams);
	}

	@PostMapping("/destroyVolume")
	public Mono<ResponseEntity<JsonNode>> destroyVolume(@RequestParam Map<String, String> queryParams) {
		return acsService.destroyVolume(queryParams);
	}

	@GetMapping("/queryAsyncJobResult") // jobid
	public Mono<ResponseEntity<JsonNode>> deleteNetwork(@RequestParam Map<String, String> param) {
		return acsService.queryAsyncJobResult(param);
	}
	
	@GetMapping("/random")
	public int generateRandomnumbers() {
	Random random = new Random();
    // Generate a random number between 1000 and 9999
    int fourDigit = 1000 + random.nextInt(9000); 
	return fourDigit;
}
	
//	@PostMapping("/keepalive")
//	public ResponseEntity<?> keepAlive(@RequestBody Map<String, String> tokens) {
//		return acsService.keepAlive(tokens);
//	}	
	@PostMapping("/keepalive")
	public ResponseEntity<?> keepAlive(@RequestBody TokenResponse tokens) {
		 System.out.println("Received access token: " + tokens.getAccessToken());
		    System.out.println("Received refresh token: " + tokens.getRefreshToken());
	    return acsService.keepAlive(tokens);
	}
	

}
