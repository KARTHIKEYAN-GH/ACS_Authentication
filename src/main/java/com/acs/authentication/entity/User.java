package com.acs.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "email")
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type")
	private UserType userType;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "account_id")
	private Long accountId;

	@Column(name = "domain_id")
	private Long domainId;

	@Column(name = "domain_uuid")
	private String domainUuid;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "secret_key")
	private String secretKey;

	@ManyToOne
	@JoinColumn(name = "domain_id", referencedColumnName = "id", insertable = false, updatable = false)
	private Domain domain;

	public enum UserType {
		DOMAIN_ADMIN, ROOT_ADMIN, USER, READ_ONLY_ADMIN;
	}
}
