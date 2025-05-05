package com.acs.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acs.authentication.handler.GenericRequestHandler;
import com.acs.web.dto.CreateNetworkRequest;
import com.acs.web.dto.CreateVolumeRequest;
import com.acs.web.dto.DeleteNetworkRequest;
import com.acs.web.dto.DeleteVolumeRequest;
import com.acs.web.dto.DestroyVolumeRequest;
import com.acs.web.dto.QueryAsyncJobResult;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/acs")
public class APIController {

	@Autowired
	private GenericRequestHandler requestHandler;

	
	@GetMapping("/createVolume")
	public Mono<ResponseEntity<String>> createVolume(@RequestBody CreateVolumeRequest request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

	@GetMapping("/deleteVolume")
	public Mono<ResponseEntity<String>> deleteNetwork(@RequestBody DeleteVolumeRequest request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

	@PostMapping("/destroyVolume")
	public Mono<ResponseEntity<String>> destroyVolume(@RequestBody DestroyVolumeRequest request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

	@GetMapping("/createNetwork")
	public Mono<ResponseEntity<String>> createNetwork(@RequestBody CreateNetworkRequest request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

	@GetMapping("/deleteNetwork")
	public Mono<ResponseEntity<String>> deleteNetwork(@RequestBody DeleteNetworkRequest request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

	@GetMapping("/queryAsyncJobResult")
	public Mono<ResponseEntity<String>> deleteNetwork(@RequestBody QueryAsyncJobResult request) {
		return requestHandler.handle(request, HttpMethod.GET);
	}

}
