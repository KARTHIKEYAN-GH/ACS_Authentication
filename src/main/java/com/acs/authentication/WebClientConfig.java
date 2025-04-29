package com.acs.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.acs.authentication.entity.AppConfiguration;
import com.acs.authentication.repo.ConfigurationRepository;

@Configuration
public class WebClientConfig {
	@Autowired
	private ConfigurationRepository configRepo;

	@Bean
	public WebClient webClient() {
		AppConfiguration config = configRepo.findTopByOrderByIdAsc();

		return WebClient.builder()
				.baseUrl(config.getBaseUrl())
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
}
