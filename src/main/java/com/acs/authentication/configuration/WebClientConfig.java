package com.acs.authentication.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.acs.authentication.entity.AppConfiguration;
import com.acs.authentication.repo.ConfigurationRepository;
import com.acs.authentication.util.SignatureUtil;

@Configuration
public class WebClientConfig {
	@Autowired
	private ConfigurationRepository configRepo;

	@Bean
	public WebClient webClient() {
		String baseUrl = configRepo.findBaseUrlByIdOne();

		return WebClient.builder().baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				// .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
				.build();
	}
}
