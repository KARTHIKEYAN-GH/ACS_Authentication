package com.acs.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Configuration")
@Data
public class AppConfiguration {

	/** Unique ID of the domain. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "secret_key")
	private String secretKey;

	@Column(name = "base_url")
	private String baseUrl;
}
