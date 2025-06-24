package com.acs.authentication.session;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "session")  // Service name from Eureka
public interface SessionClient {
		
	@GetMapping("api/session/read/{key}")
	String getSessionInfo(@PathVariable("key") String key);
}
